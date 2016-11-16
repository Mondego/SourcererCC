package com.mondego.models;
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return token.hashCode();
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
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.getValue().equals(other.token.getValue())) {
            return false;
        }
        return true;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TokenFrequency [frequency=" + frequency + ", token=" + token
                + "]";
    }
    
    
    
}
