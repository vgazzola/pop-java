package test;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.baseobject.ConnectionType;

@POPClass
public class Toto {
	private int identity;

	@POPObjectDescription(url="localhost")
	public Toto(){
	}
	
	@POPSyncConc
	public void setIdent(int i){
		identity = i;
	}
	
	@POPSyncSeq
	public int getIdent(){
		Titi t = new Titi();
		setIdent(222);
		t.setToto(this);
		t.computeIdent();
		return identity;
	}
}

