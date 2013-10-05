/**
 * 
 */

/**
 * @author vaibhavsaini
 *
 */
public class TokenFrequency {
    private int frequency;
    private Token token;
    private int tokenPosition;
    /**
     * @return the frequency
     */
    public int getFrequency() {
        return frequency;
    }
    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    /**
     * @return the token
     */
    public Token getToken() {
        return token;
    }
    /**
     * @param token the token to set
     */
    public void setToken(Token token) {
        this.token = token;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TokenFrequency [frequency=" + frequency + ", token=" + token
                + "]";
    }
    /**
     * @return the tokenPosition
     */
    public int getTokenPosition() {
        return tokenPosition;
    }
    /**
     * @param tokenPosition the tokenPosition to set
     */
    public void setTokenPosition(int tokenPosition) {
        this.tokenPosition = tokenPosition;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + tokenPosition;
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TokenFrequency)) {
            return false;
        }
        TokenFrequency other = (TokenFrequency) obj;
        if (tokenPosition != other.tokenPosition) {
            return false;
        }
        return true;
    }
}
