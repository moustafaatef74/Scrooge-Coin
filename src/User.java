import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class User {
	PublicKey publicKey;
	PrivateKey privateKey;
	ArrayList<Transaction> ingoingTransactions;
	ArrayList<Transaction> outgoingTransactions;
	ArrayList<Coin> ownedCoins;
	String toBePrinted;
	public User() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
		
        this.publicKey = keyPair.getPublic();
		this.privateKey = keyPair.getPrivate();
		this.ingoingTransactions = new ArrayList<Transaction>();
		this.outgoingTransactions = new ArrayList<Transaction>();
		this.ownedCoins = new ArrayList<Coin>();

	}
	public void generateToBePrinted() {
		toBePrinted = "Public Key :" + publicKey+
				"\nNumber of owned coins: " + ownedCoins.size()+
				"\n------------------------------------------------------------";
	}

}
