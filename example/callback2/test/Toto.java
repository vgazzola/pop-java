package test;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;

@POPClass
public class Toto {
	private int identity;

	public Toto() {
	}
	
	@POPSyncConc
	public void setIdent(int i){
		identity = i;
	}
	
	@POPSyncSeq
	public int getIdent(){
		Titi t = new Titi();
		setIdent(222);
		t.computeIdent(this);
		return identity;
	}
}

