package me.assil.roadomatic;

/*
    Notes:

    - Buffers are of type `char` not `byte` to prevent signed arithmetic errors

    Usage:

    import me.assil.roadomatic.XOREncrypt;
    
    // Encrypt some text
    String plain = "I want to be encrypted.";
    String cipher = XOREncrypt.encrypt(plain);
    
    // Try getting it back
    String recovered = XOREncrypt.decrypt(cipher);

    // Should print "Yes"
    if (recovered.equals(plain))
        System.out.println("Yes");
*/
public class XOREncrypt {
    public static String encrypt(String plain) {
        // Generate one-time XOR key
        char key = (char)(Math.floor((Math.random()*254)) + 1);

        // Allocate buffer to store ciphertext
        char[] buf = new char[plain.length()+1];
        int bufSize = buf.length;

        // Append key to buffer
        buf[bufSize-1] = key;

        // Perform encryption
        for (int i = 0; i < bufSize-1; i++)
            buf[i] = (char)(plain.charAt(i) ^ key);

        return new String(buf);
    }

    public static String decrypt(String cipher) {
        // Allocate buffer to store plaintext
        char[] buf = new char[cipher.length()-1];

        // Retrieve key
        char key = cipher.charAt(cipher.length()-1);

        for (int i = 0; i < buf.length; i++)
            buf[i] = (char)(cipher.charAt(i) ^ key);

        return new String(buf);
    }
}
