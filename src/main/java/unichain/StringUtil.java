package unichain;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

public class StringUtil {

    public static String applySHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Errore: "+e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public static byte[] applyECDSASignature(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] realSignature;
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            realSignature = dsa.sign();
        } catch (Exception e) {
            System.out.println("Errore: "+e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        return realSignature;
    }

    public static boolean verifyECDSASignature(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            System.out.println("Errore: "+e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while (count > 1) {
            treeLayer = new ArrayList<>();
            for (int i=1; i< previousTreeLayer.size(); i++) {
                treeLayer.add(
                        applySHA256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i))
                );
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        return treeLayer.size()==1 ? treeLayer.get(0) : ""; // <-- Questo è il Merkle Root
    }

}
