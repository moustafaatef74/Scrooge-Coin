import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.Date;

public class Transaction {
	int ID;
	Timestamp timestamp;
	Coin coin;
	PublicKey receiver;
	PublicKey sender;
	byte[] senderSignture;
	String Hash;
	String previousHash;
	String toBePrinted;
	public Transaction(int ID, Coin coin, PublicKey reciever, PublicKey sender) {
		this.ID = ID;
		this.coin = coin;
//		this.transactionValue = transactionValue;
		this.receiver = reciever;
		this.sender = sender;
		this.timestamp = new Timestamp(new Date().getTime());
/*		if(coin.value == this.transactionValue) {
			consumed = false;
		}
		else {
			consumed = true;
		}*/
		
		
	}
	public String generateHash() {
		
		String input = "ID: " +this.ID+
				"\n Time: " + this.timestamp +
				"\n Coin ID: " + this.coin.coinID+
				"\n Coin Value: " + this.coin.value+
				"\n Reciever: " + this.receiver+
				"\n Sender: " + this.sender +
				"\n Previous Hash: " + this.previousHash ;
		
		//System.out.println(input);
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        byte [] hash = md.digest(input.getBytes(StandardCharsets.UTF_8)); 
        BigInteger number = new BigInteger(1, hash);  
  	  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        } 
        this.Hash = hexString.toString();
        
        return hexString.toString();
	}
	public void signTransaction(PrivateKey senderPrivateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance("SHA256withDSA");
		signature.initSign(senderPrivateKey);
        byte[] stringToBytes = generateHash().getBytes();
        signature.update(stringToBytes);
        byte [] senderSignature = signature.sign();
        this.senderSignture = senderSignature;
        this.toBePrinted = 
        		"\nTransaction-ID: " +this.ID+
				"\nTime: " + this.timestamp +
				"\nCoin ID: " + this.coin.coinID+
				"\nCoin Value: " + this.coin.value+
				"\nPrevious Hash: " + this.previousHash +
				"\nHash: " + this.Hash+
				"\nReciever: " + this.receiver+
				"\nSender: " + this.sender +
				"\n------------------------------------------------------------------------------------";
	}
	public boolean validateSignture() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		
		Signature signature = Signature.getInstance("SHA256withDSA");
		signature.initVerify(this.sender);
		signature.update(generateHash().getBytes());
		return signature.verify(this.senderSignture);
	}
/*	public boolean validateTransaction() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		if(this.coin.value>=this.transactionValue) {
			if(validateSignture()) {
				
				return true;
				
			}
		}
		return false;
	}*/
	
	public static void main(String[]args) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		
	}
}
