import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Block {
	int ID;
	ArrayList<Transaction> Data;
	int maxSize = 10;
	String Hash;
	String previousHash;
	String toBePrinted;
	
	public Block(int ID) {
		this.ID = ID;
		this.previousHash = null;
		this.Data = new ArrayList<Transaction>();
	}
	public void generateHash() throws NoSuchAlgorithmException {
		String input = "ID: " +this.ID+
				"\n PreviousHash: " + this.previousHash +
				"\n Data: " + this.Data;
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");  
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
        this.toBePrinted = "Block-ID: " +this.ID+
				"\n PreviousHash: " + this.previousHash +
				"\n Hash: " + this.Hash +
				"\n----------------------------------";
	}
	public static void main(String[]args) throws NoSuchAlgorithmException {

	}
}
