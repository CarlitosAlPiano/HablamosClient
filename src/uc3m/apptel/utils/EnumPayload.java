package uc3m.apptel.utils;

public enum EnumPayload {
	PAYLOAD_TEXT		(01),
	PAYLOAD_HTML		(02),
	PAYLOAD_PIC_JPEG	(03),
	PAYLOAD_PIC_PNG		(04),
	PAYLOAD_INT			(05),
	PAYLOAD_FLOAT		(06),
	PAYLOAD_EMPTY		(20);

	private int pld;

	private EnumPayload(int pld) {
		this.pld = pld;
	}
	
	public int getValue() {
		return pld;
	}
	
	public static EnumPayload fromInt(int pld) {
		EnumPayload[] values = EnumPayload.values();

        for(int i=0; i<values.length; i++) {
            if(values[i].getValue() == pld) return values[i];
        }
        
        return PAYLOAD_EMPTY;
	}

	@Override
	public String toString() {
		// return this.name();
		switch(this){
		case PAYLOAD_TEXT:
			return "Text";
		case PAYLOAD_HTML:
			return "Html";
		case PAYLOAD_PIC_JPEG:
			return "Jpeg";
		case PAYLOAD_PIC_PNG:
			return "Png";
		case PAYLOAD_INT:
			return "Int";
		case PAYLOAD_FLOAT:
			return "Float";
		case PAYLOAD_EMPTY:
		default:
			return "Empty";
		}
	}
}
