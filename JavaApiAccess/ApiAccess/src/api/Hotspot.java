package api;

public enum Hotspot {

	MANCHESTER,
	LIVERPOOL,
	NEWCASTLE,
	CUMBRIA,
	EDINBURGH,
	BIRMINGHAM,
	BRIGHTON,
	LONDON,
	GLASGOW,
	SHEFFIELD,
	BELFAST,
	BLACKPOOL,
	CAMBRIDGE,
	CARDIFF;
	
	@Override
	public String toString() {
		switch (this) {
		case MANCHESTER:
			return "Manchester";
		case CUMBRIA:
			return "Cumbria";
		case BELFAST:
			return "Belfast";
		case BIRMINGHAM:
			return "Birmingham";
		case BLACKPOOL:
			return "Blackpool";
		case BRIGHTON:
			return "Brighton";
		case CAMBRIDGE:
			return "Cambridge";
		case CARDIFF:
			return "Cardiff";
		case EDINBURGH:
			return "Edinburgh";
		case GLASGOW:
			return "Glasgow";
		case LIVERPOOL:
			return "Liverpool";
		case LONDON:
			return "London";
		case NEWCASTLE:
			return "Newcastle";
		case SHEFFIELD:
			return "Sheffield";
		default:
			return "";
		}
	}
	
	public String areaCode() {
		switch(this) {
		case MANCHESTER:
			return "E08000003";
		case BELFAST:
			return "N09000003";
		case BIRMINGHAM:
			return "E08000025";
		case BLACKPOOL:
			return "E08000025";
		case BRIGHTON:
			return "E06000043";
		case CAMBRIDGE:
			return "E10000003";
		case CARDIFF:
			return "W06000015";
		case CUMBRIA:
			return "E10000006";
		case EDINBURGH:
			return "S12000036";
		case GLASGOW:
			return "S12000049";
		case LIVERPOOL:
			return "E08000012";
		case LONDON:
			return "E12000007";
		case NEWCASTLE:
			return "E08000021";
		case SHEFFIELD:
			return "E08000019";
		default:
			return "";
			
		}
	}
}
