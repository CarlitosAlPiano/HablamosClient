package uc3m.apptel.utils;

public enum EnumCommand {
	CMD_SEND					(01),
	SUCC_ANSWER_TO_SEND			(02),
	ERR_ANSWER_TO_SEND			(03),
	CMD_DELIVERED				(11),
	SUCC_ANSWER_TO_DELIVERED	(12),
	ERR_ANSWER_TO_DELIVERED		(13),
	CMD_REGISTER				(21),
	SUCC_ANSWER_TO_REGISTER		(22),
	ERR_ANSWER_TO_REGISTER		(23),
	CMD_UNREGISTER				(31),
	SUCC_ANSWER_TO_UNREGISTER	(32),
	ERR_ANSWER_TO_UNREGISTER	(33),
	UNKNOWN						(-1);

	private int cmd;

	private EnumCommand(int cmd) {
		this.cmd = cmd;
	}
	
	public int getValue() {
		return cmd;
	}
	
	public static EnumCommand fromInt(int cmd) {
		EnumCommand[] values = EnumCommand.values();

        for(int i=0; i<values.length; i++) {
            if(values[i].getValue() == cmd) return values[i];
        }
        
        return UNKNOWN;
	}

	@Override
	public String toString() {
		/*if(this == UNKNOWN){
			return "Unknown command";
		}else{
			return this.name();
		}*/
		switch(this){
		case CMD_SEND:
			return "Send";
		case SUCC_ANSWER_TO_SEND:
			return "SendOk";
		case ERR_ANSWER_TO_SEND:
			return "SendFail";
		case CMD_DELIVERED:
			return "Delivered";
		case SUCC_ANSWER_TO_DELIVERED:
			return "DeliveredOk";
		case ERR_ANSWER_TO_DELIVERED:
			return "DeliveredFail";
		case CMD_REGISTER:
			return "Register";
		case SUCC_ANSWER_TO_REGISTER:
			return "RegisterOk";
		case ERR_ANSWER_TO_REGISTER:
			return "RegisterFail";
		case CMD_UNREGISTER:
			return "Unregister";
		case SUCC_ANSWER_TO_UNREGISTER:
			return "UnregisterOk";
		case ERR_ANSWER_TO_UNREGISTER:
			return "UnregisterFail";
		case UNKNOWN:
		default:
			return "Unknown command";
		}
	}
}
