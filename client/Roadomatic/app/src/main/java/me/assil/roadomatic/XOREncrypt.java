package me.assil.roadomatic;

/*
    Usage:

    XOREncrypt x = new XOREncrypt();
    
    // Encrypt some text
    String plain = "I want to be encrypted.";
    String cipher = x.encrypt(plain);
    
    // Try getting it back
    String recovered = x.decrypt(cipher);

    // Should print "Yes"
    if (recovered.equals(plain))
        System.out.println("Yes");
*/
public class XOREncrypt {
    public String encrypt(String plain) {
        // Generate one-time XOR key
        byte key = (byte)(Math.floor((Math.random()*254)) + 1);

        // Allocate buffer to store ciphertext
        byte[] buf = new byte[plain.length()+1];
        int bufSize = buf.length;

        // Append key to buffer
        buf[bufSize-1] = key;

        // Perform encryption
        for (int i = 0; i < bufSize-1; i++)
            buf[i] = (byte)(plain.charAt(i) ^ key);

        return new String(buf);
    }

    public String decrypt(String cipher) {
        // Allocate buffer to store plaintext
        byte[] buf = new byte[cipher.length()-1];

        // Retrieve key
        int key = cipher.charAt(cipher.length()-1);

        for (int i = 0; i < buf.length; i++)
            buf[i] = (byte)(cipher.charAt(i) ^ key);

        return new String(buf);
    }
}
