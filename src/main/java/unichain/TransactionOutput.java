package unichain;

import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey recipient;
    public float value;
    public String parentTransactionId;

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySHA256(StringUtil.getStringFromKey(recipient) +
                value + parentTransactionId);
    }

    public boolean isMine(PublicKey publicKey) {
        return publicKey == recipient;
    }
}
