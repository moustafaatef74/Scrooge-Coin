import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Scrooge extends JFrame implements KeyListener {
	ArrayList<User> Users = new ArrayList<User>();
	PublicKey publicKey;
	PrivateKey privateKey;
	Blockchain ledger = new Blockchain();
	Block currentBlock = new Block(0);
	ArrayList<Coin> ownedCoins = new ArrayList<Coin>();
	int currentCoin = 0;
	int currentTransaction =0;
	boolean initialCoins =true;
	File fout;
	FileOutputStream fos;
	BufferedWriter bw;
	//------------------------
	JPanel p = new JPanel();
	JLabel label;
	public Scrooge() throws NoSuchAlgorithmException, FileNotFoundException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
		
        this.publicKey = keyPair.getPublic();
		this.privateKey = keyPair.getPrivate();
		//------------------------------
		
		
        label = new JLabel("Key Listener!");
        p.add(label);
        add(p);
        addKeyListener(this);
        setSize(200, 100);
        setVisible(true);
        
        this.fout = new File("D:\\GUC\\Semester 10\\Security\\Project\\ScroogeCoin\\src\\Output.txt");
		this.fos = new FileOutputStream(fout);
		this.bw = new BufferedWriter(new OutputStreamWriter(fos));
	}
	
	public void createUsers(int numberOfUsers) throws NoSuchAlgorithmException {
		for(int i = 0; i<numberOfUsers;i++) {
			User u = new User();
			Users.add(u);
		}
		for(int i = 0; i<numberOfUsers;i++) {
			for(int j = 0; j<numberOfUsers;j++) {
				if(Users.get(i).publicKey == Users.get(j).publicKey && i!=j) {
					Users.set(j, new User());
					i =0;
					j =0;
				}
			}
		}
	}
	
	public void intialCoins(int initialCoinsValue) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		
		for(int i = 0; i<Users.size();i++) {
			for(int j =0; j<10;j++) {
				this.ownedCoins.add(new Coin(1,currentCoin));
				Transaction t = new Transaction(currentTransaction, ownedCoins.get(0), Users.get(i).publicKey, this.publicKey);
				t.previousHash = getPreviousTransactionHash(t.coin);
				t.signTransaction(this.privateKey);
				currentBlock.Data.add(t);
				Users.get(i).ingoingTransactions.add(t);
				this.ownedCoins.remove(0);
				Users.get(i).ownedCoins.add(t.coin);
				//Printing
/*				bw.write("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.newLine();
				for(int k =0; k<currentBlock.Data.size(); k++) {
					bw.write(currentBlock.Data.get(k).toBePrinted);
					bw.newLine();
				}*/
				
				if(currentBlock.Data.size()==currentBlock.maxSize) {
					blockHandler();
				}
				currentCoin+=1;
				currentTransaction+=1;
				
			}
			Users.get(i).generateToBePrinted();
			System.out.println(Users.get(i).toBePrinted);
			bw.write(Users.get(i).toBePrinted);
			bw.newLine();
			
		}
		initialCoins = false;
	}
	
	public void blockHandler() throws NoSuchAlgorithmException, IOException {
		
		if(ledger.Data.size()==0) {
			currentBlock.previousHash = null;
		}
		else {
			currentBlock.previousHash = ledger.Data.get(ledger.Data.size()-1).Hash;
			currentBlock.ID = ledger.Data.get(ledger.Data.size()-1).ID+1;
		}
		currentBlock.generateHash();
		ledger.Data.add(currentBlock);
		currentBlock = new Block(10);
		
		
		// PRINTING
		if(!initialCoins) {
			System.out.println("******************************************************************\n"+
					"******************************************************************\n"+
					"************The blockchain ledger contains these blocks:************\n"+
					"******************************************************************\n"+
				    "******************************************************************\n");
			bw.write("******************************************************************\n"+
					"******************************************************************\n"+
					"************The blockchain ledger contains these blocks:************\n"+
					"******************************************************************\n"+
				    "******************************************************************\n");
			bw.newLine();
			for(int i = 0;i<ledger.Data.size();i++) {
				System.out.println(ledger.Data.get(i).toBePrinted);
				bw.write(ledger.Data.get(i).toBePrinted);
				bw.newLine();
			}
		}
		
	}
	
	public String getPreviousTransactionHash(Coin c) {
		String latestTransaction = null;
		for(int i = 0; i<ledger.Data.size();i++) {
			for(int j = 0; j<ledger.Data.get(i).Data.size();j++) {
				if(c.coinID == ledger.Data.get(i).Data.get(j).coin.coinID) {
					latestTransaction = ledger.Data.get(i).Data.get(j).Hash;
				}
			}
		}
		for(int i = 0; i<currentBlock.Data.size();i++) {
			if(c.coinID == currentBlock.Data.get(i).coin.coinID) {
				latestTransaction = currentBlock.Data.get(i).Hash;
			}
		}
		return latestTransaction;
	}
	
	public boolean validateTransaction(Transaction t, ArrayList<Coin> c) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		if(!t.validateSignture()) {
			System.out.println("\n**************************** Invalid Signature transaction ****************************\n"
					+t.toBePrinted+
					"\n**************************** Invalid Signature transaction ****************************\n");
			bw.write("\n**************************** Invalid Signature transaction ****************************\n"
					+t.toBePrinted+
					"\n**************************** Invalid Signature transaction ****************************\n");
			bw.newLine();
			return false;
		}
		if(!c.contains(t.coin)) {
			System.out.println("\n**************************** Invalid Owner transaction ****************************\n"
					+t.toBePrinted+
					"\n**************************** Invalid Owner transaction ****************************\n");
			bw.write("\n**************************** Invalid Owner transaction ****************************\n"
					+t.toBePrinted+
					"\n**************************** Invalid Owner transaction ****************************\n");
			bw.newLine();
			return false;
		}
		return true;
	}
	public void simulateTransactions() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		Random r = new Random();
		boolean doubleSpending = false;
		for(int i =0; i<5 ;i++) {
			User sender = Users.get(r.nextInt(100));
			User receiver = Users.get(r.nextInt(100));
			while(sender.equals(receiver)) {
				receiver = Users.get(r.nextInt(100));
			}
			Transaction t = new Transaction(currentTransaction,sender.ownedCoins.get(r.nextInt(sender.ownedCoins.size())),receiver.publicKey,sender.publicKey);
			//getting the previous hash for transaction
			for(int l = 0; l<ledger.Data.size();l++) {
				for(int b =0; b<ledger.Data.get(l).Data.size();b++) {
					if(ledger.Data.get(l).Data.get(b).coin.coinID == t.coin.coinID) {
						t.previousHash = ledger.Data.get(l).Data.get(b).Hash;
					}
				}
			}
			//------------------------------------------
			t.signTransaction(sender.privateKey);
			
			doubleSpending = checkForDoubleSpending(t);
			
			if(validateTransaction(t, sender.ownedCoins) &&!doubleSpending) {
				currentBlock.Data.add(t);
				System.out.println("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.write("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.newLine();
				for(int k =0; k<currentBlock.Data.size(); k++) {
					System.out.println(currentBlock.Data.get(k).toBePrinted);
					bw.write(currentBlock.Data.get(k).toBePrinted);
					bw.newLine();
				}
				if(currentBlock.Data.size()==currentBlock.maxSize) {
					blockHandler();
					changeCoinOwnership();
				}
				currentTransaction+=1;
			}
			
			
			doubleSpending = false;
			
		}
	}
	public void simulateDoubleSpendingTransactions() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		Random r = new Random();
		boolean doubleSpending = false;
		User sender = Users.get(r.nextInt(100));
		Coin c = sender.ownedCoins.get(r.nextInt(sender.ownedCoins.size()));
		for(int i =0; i<2 ;i++) {
			User receiver = Users.get(r.nextInt(100));
			while(sender.equals(receiver)) {
				receiver = Users.get(r.nextInt(100));
			}
			Transaction t = new Transaction(currentTransaction,c,receiver.publicKey,sender.publicKey);
			for(int l = 0; l<ledger.Data.size();l++) {
				for(int b =0; b<ledger.Data.get(l).Data.size();b++) {
					if(ledger.Data.get(l).Data.get(b).coin.coinID == t.coin.coinID) {
						t.previousHash = ledger.Data.get(l).Data.get(b).Hash;
					}
				}
			}
			//------------------------------------------
			t.signTransaction(sender.privateKey);
			
			doubleSpending = checkForDoubleSpending(t);
			
			if(validateTransaction(t, sender.ownedCoins) &&!doubleSpending) {
				currentBlock.Data.add(t);
				System.out.println("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.write("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.newLine();
				for(int k =0; k<currentBlock.Data.size(); k++) {
					System.out.println(currentBlock.Data.get(k).toBePrinted);
					bw.write(currentBlock.Data.get(k).toBePrinted);
					bw.newLine();
				}
				if(currentBlock.Data.size()==currentBlock.maxSize) {
					blockHandler();
					changeCoinOwnership();
				}
				currentTransaction+=1;
			}
			

			doubleSpending = false;
			
		}
	}
	public void simulateInvalidSignatureTransactions() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		Random r = new Random();
		boolean doubleSpending = false;
		User sender = Users.get(r.nextInt(100));
		User receiver = Users.get(r.nextInt(100));
		Coin c = sender.ownedCoins.get(r.nextInt(sender.ownedCoins.size()));
		User invalidSigner = Users.get(r.nextInt(100));
		while(sender.equals(receiver) || sender.equals(invalidSigner)) {
			receiver = Users.get(r.nextInt(100));
		}
		Transaction t = new Transaction(currentTransaction,c,receiver.publicKey,sender.publicKey);
		for(int l = 0; l<ledger.Data.size();l++) {
			for(int b =0; b<ledger.Data.get(l).Data.size();b++) {
				if(ledger.Data.get(l).Data.get(b).coin.coinID == t.coin.coinID) {
					t.previousHash = ledger.Data.get(l).Data.get(b).Hash;
				}
			}
		}
		//------------------------------------------
		t.signTransaction(invalidSigner.privateKey);
		
		
		
		if(validateTransaction(t, sender.ownedCoins)) {
			doubleSpending = checkForDoubleSpending(t);
			if(!doubleSpending) {
				currentBlock.Data.add(t);
				System.out.println("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.write("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.newLine();
				for(int k =0; k<currentBlock.Data.size(); k++) {
					System.out.println(currentBlock.Data.get(k).toBePrinted);
					bw.write(currentBlock.Data.get(k).toBePrinted);
					bw.newLine();
				}
				if(currentBlock.Data.size()==currentBlock.maxSize) {
					blockHandler();
					changeCoinOwnership();
				}
				currentTransaction+=1;
			}
		}
		
		
		doubleSpending = false;
	}
	public void simulateInvalidOwnerTransactions() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		Random r = new Random();
		boolean doubleSpending = false;
		User sender = Users.get(r.nextInt(100));
		User receiver = Users.get(r.nextInt(100));
		Coin c = sender.ownedCoins.get(r.nextInt(sender.ownedCoins.size()));
		User invalidOwner = Users.get(r.nextInt(100));
		while(sender.equals(receiver) || sender.equals(invalidOwner)) {
			receiver = Users.get(r.nextInt(100));
		}
		Transaction t = new Transaction(currentTransaction,c,receiver.publicKey,invalidOwner.publicKey);
		for(int l = 0; l<ledger.Data.size();l++) {
			for(int b =0; b<ledger.Data.get(l).Data.size();b++) {
				if(ledger.Data.get(l).Data.get(b).coin.coinID == t.coin.coinID) {
					t.previousHash = ledger.Data.get(l).Data.get(b).Hash;
				}
			}
		}
		//------------------------------------------
		t.signTransaction(invalidOwner.privateKey);
		
		
		
		if(validateTransaction(t, invalidOwner.ownedCoins)) {
			doubleSpending = checkForDoubleSpending(t);
			if(!doubleSpending) {
				currentBlock.Data.add(t);
				System.out.println("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.write("******************************************\n"+"****Block under construction contains:****\n"+"******************************************\n");
				bw.newLine();
				for(int k =0; k<currentBlock.Data.size(); k++) {
					System.out.println(currentBlock.Data.get(k).toBePrinted);
					bw.write(currentBlock.Data.get(k).toBePrinted);
					bw.newLine();
				}
				if(currentBlock.Data.size()==currentBlock.maxSize) {
					blockHandler();
					changeCoinOwnership();
				}
				currentTransaction+=1;
			}
		}
		
		
		doubleSpending = false;
	}
	public void changeCoinOwnership() {
		Block lastBlock = ledger.Data.get(ledger.Data.size()-1);
		for(int i=0;i<lastBlock.Data.size();i++) {
			User sender = null;
			User receiver = null;
			for(int j = 0; j<Users.size();j++) {
				if(sender!=null && receiver!=null) {
					break;
				}
				if(lastBlock.Data.get(i).sender == Users.get(j).publicKey) {
					sender = Users.get(j);
				}
				if(lastBlock.Data.get(i).receiver == Users.get(j).publicKey) {
					receiver = Users.get(j);
				}
				
			}
			sender.outgoingTransactions.add(lastBlock.Data.get(i));
			receiver.ingoingTransactions.add(lastBlock.Data.get(i));
			sender.ownedCoins.remove(lastBlock.Data.get(i).coin);
			receiver.ownedCoins.add(lastBlock.Data.get(i).coin);
			sender.generateToBePrinted();
			receiver.generateToBePrinted();
		}
		
	}

	public boolean checkForDoubleSpending(Transaction t) throws IOException {
		boolean doubleSpending = false;
		for(int j = 0; j<currentBlock.Data.size();j++) {
			if(currentBlock.Data.get(j).coin.coinID == t.coin.coinID) {
				System.out.println("\n**************************** Invalid Double-spending transaction ****************************\n"
						+t.toBePrinted+
						"\n**************************** Invalid Double-spending transaction ****************************\n");
				bw.write("\n**************************** Invalid Double-spending transaction ****************************\n"
						+t.toBePrinted+
						"\n**************************** Invalid Double-spending transaction ****************************\n");
				bw.newLine();
				
				doubleSpending = true;
			}
		}
		return doubleSpending;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_A) {
			try {
				simulateTransactions();
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		if(e.getKeyCode() == KeyEvent.VK_S) {
			try {
				simulateDoubleSpendingTransactions();
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			try {
				simulateInvalidSignatureTransactions();
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		if(e.getKeyCode() == KeyEvent.VK_F) {
			try {
				simulateInvalidOwnerTransactions();
			} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			try {
				bw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.exit(0);
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[]args)  throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		Scrooge s = new Scrooge();
		s.createUsers(100);
		s.intialCoins(10);
		System.out.println("to Simulate 5 valid transactions press A");
		System.out.println("to Simulate a double spending attack press S");
		System.out.println("to Simulate an invalid signature transaction press D");
		System.out.println("to Simulate an invalid owner transaction  press F");
		System.out.println("to quit press SPACE");

	}

	
}
