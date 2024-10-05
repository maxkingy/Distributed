package weatherService;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeatherDataEntryTest {

	@Test
	@DisplayName("Set and get ID test")
	void setID() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setID("IDS60901");
		assertEquals("IDS60901", entry.getID());
	}

	@Test
	@DisplayName("Set and get name test")
	void setName() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setName("Adelaide (West Terrace /  ngayirdapira)");
		assertEquals("Adelaide (West Terrace /  ngayirdapira)", entry.getName());
	}

	@Test
	@DisplayName("Set and get state test")
	void setState() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setState("SA");
		assertEquals("SA", entry.getState());
	}

	@Test
	@DisplayName("Set and get time zone test")
	void setTime_zone() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setTime_zone("CST");
		assertEquals("CST", entry.getTime_zone());
	}

	@Test
	@DisplayName("Set and get latitude test")
	void setLat() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setLat(-34.9);
		assertEquals(-34.9, entry.getLat());
	}

	@Test
	@DisplayName("Set and get longitude test")
	void setLon() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setLon(138.6);
		assertEquals(138.6, entry.getLon());
	}

	@Test
	@DisplayName("Set and get local date and time test")
	void setLocal_date_time() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setLocal_date_time("16/05:00pm");
		assertEquals("16/05:00pm", entry.getLocal_date_time());
	}

	@Test
	@DisplayName("Set and get local date and time full test")
	void setLocal_date_time_full() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setLocal_date_time_full("20230716170000");
		assertEquals("20230716170000", entry.getLocal_date_time_full());
	}

	@Test
	@DisplayName("Set and get air temperature test")
	void setAir_temp() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setAir_temp(19.0);
		assertEquals(19.0, entry.getAir_temp());
	}

	@Test
	@DisplayName("Set and get apparent temperature test")
	void setApparent_t() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setApparent_t(15.0);
		assertEquals(15.0, entry.getApparent_t());
	}

	@Test
	@DisplayName("Set and get cloudiness test")
	void setCloud() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setCloud("Sunny");
		assertEquals("Sunny", entry.getCloud());
	}

	@Test
	@DisplayName("Set and get dew point test")
	void setDewpt() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setDewpt(5.7);
		assertEquals(5.7, entry.getDewpt());
	}

	@Test
	@DisplayName("Set and get pressure test")
	void setPress() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setPress(1023.9);
		assertEquals(1023.9, entry.getPress());
	}

	@Test
	@DisplayName("Set and get relative humidity test")
	void setRel_hum() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setRel_hum(60.0);
		assertEquals(60.0, entry.getRel_hum());
	}

	@Test
	@DisplayName("Set and get wind direction test")
	void setWind_dir() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setWind_dir("S");
		assertEquals("S", entry.getWind_dir());
	}

	@Test
	@DisplayName("Set and get wind speed in kilometers per hour test")
	void setWind_spd_kmh() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setWind_spd_kmh(15.0);
		assertEquals(15.0, entry.getWind_spd_kmh());
	}

	@Test
	@DisplayName("Set and get wind speed in knots test")
	void setWind_spd_kt() {
		WeatherDataEntry entry = new WeatherDataEntry();
		entry.setWind_spd_kt(8.0);
		assertEquals(8.0, entry.getWind_spd_kt());
	}
}