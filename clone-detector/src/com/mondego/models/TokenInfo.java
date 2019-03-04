package com.mondego.models;

public class TokenInfo {
	private int frequency;
	private int position;
	
	
	public TokenInfo(int frequency) {
		super();
		this.frequency = frequency;
		this.position=-1;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	@Override
	public String toString() {
		return "TokenInfo [frequency=" + frequency + ", position=" + position
				+ "]";
	}
}
