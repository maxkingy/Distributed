package weatherService;

public class WeatherDataEntry {

	private String id;
	private String name;
	private String state;
	private String time_zone;
	private Double lat;
	private Double lon;
	private String local_date_time;
	private String local_date_time_full;
	private Double air_temp;
	private Double apparent_t;
	private String cloud;
	private Double dewpt;
	private Double press;
	private Double rel_hum;
	private String wind_dir;
	private Double wind_spd_kmh;
	private Double wind_spd_kt;

	/*
	 * Simple getters and setters for each weather data variable. Each setter takes
	 * an input argument corresponding to the variable. Each getter returns the
	 * variable.
	 */

	public void setID(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTime_zone(String time_zone) {
		this.time_zone = time_zone;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

	public void setLocal_date_time(String local_date_time) {
		this.local_date_time = local_date_time;
	}

	public void setLocal_date_time_full(String local_date_time_full) {
		this.local_date_time_full = local_date_time_full;
	}

	public void setAir_temp(Double air_temp) {
		this.air_temp = air_temp;
	}

	public void setApparent_t(Double apparent_t) {
		this.apparent_t = apparent_t;
	}

	public void setCloud(String cloud) {
		this.cloud = cloud;
	}

	public void setDewpt(Double dewpt) {
		this.dewpt = dewpt;
	}

	public void setPress(Double press) {
		this.press = press;
	}

	public void setRel_hum(Double rel_hum) {
		this.rel_hum = rel_hum;
	}

	public void setWind_dir(String wind_dir) {
		this.wind_dir = wind_dir;
	}

	public void setWind_spd_kmh(Double wind_spd_kmh) {
		this.wind_spd_kmh = wind_spd_kmh;
	}

	public void setWind_spd_kt(Double wind_spd_kt) {
		this.wind_spd_kt = wind_spd_kt;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getState() {
		return state;
	}

	public String getTime_zone() {
		return time_zone;
	}

	public Double getLat() {
		return lat;
	}

	public Double getLon() {
		return lon;
	}

	public String getLocal_date_time() {
		return local_date_time;
	}

	public String getLocal_date_time_full() {
		return local_date_time_full;
	}

	public Double getAir_temp() {
		return air_temp;
	}

	public Double getApparent_t() {
		return apparent_t;
	}

	public String getCloud() {
		return cloud;
	}

	public Double getDewpt() {
		return dewpt;
	}

	public Double getPress() {
		return press;
	}

	public Double getRel_hum() {
		return rel_hum;
	}

	public String getWind_dir() {
		return wind_dir;
	}

	public Double getWind_spd_kmh() {
		return wind_spd_kmh;
	}

	public Double getWind_spd_kt() {
		return wind_spd_kt;
	}
}