package main;

import org.json.JSONObject;

public class Parser {
	/*
	 * example output
	 * {"meta":{"code":200},"response":{"holidays":[{"name":"Epiphany","description":"Epiphany on January 6 is a public holiday in 3 German states  and commemorates the Bible story of  the Magi's visit to baby Jesus.","date":{"iso":"2019-01-06","datetime":{"year":2019,"month":1,"day":6}},"type":["Christian","Common local holiday"],"locations":"BW, BY, ST","states":[{"id":166,"abbrev":"BW","name":"Baden-W\u00fcrttemberg","exception":null,"iso":"de-bw"},{"id":167,"abbrev":"BY","name":"Bavaria","exception":null,"iso":"de-by"},{"id":179,"abbrev":"ST","name":"Saxony-Anhalt","exception":null,"iso":"de-st"}]},{"name":"Corpus Christi","description":"Corpus Christi celebrates the Eucharist and is public holiday in some parts of Germany.","date":{"iso":"2019-06-20","datetime":{"year":2019,"month":6,"day":20}},"type":["Christian","Common local holiday"],"locations":"BW, BY, HE, NRW, RLP, SL","states":[{"id":166,"abbrev":"BW","name":"Baden-W\u00fcrttemberg","exception":null,"iso":"de-bw"},{"id":167,"abbrev":"BY","name":"Bavaria","exception":null,"iso":"de-by"},{"id":172,"abbrev":"HE","name":"Hesse","exception":null,"iso":"de-he"},{"id":175,"abbrev":"NRW","name":"North Rhine-Westphalia","exception":null,"iso":"de-nw"},{"id":176,"abbrev":"RLP","name":"Rhineland-Palatinate","exception":null,"iso":"de-rp"},{"id":177,"abbrev":"SL","name":"Saarland","exception":null,"iso":"de-sl"}]},{"name":"Peace Festival in Augsburg","description":"The Augsburg Peace Festival in Germany celebrates the implementation of the Peace of Westphalia in 1648 CE. It is on August 8 each year.","date":{"iso":"2019-08-08","datetime":{"year":2019,"month":8,"day":8}},"type":["Local holiday"],"locations":"BY (Augsburg)","states":[{"id":167,"abbrev":"BY","name":"Bavaria","exception":"Augsburg","iso":"de-by"}]},{"name":"Assumption of Mary","description":"The Feast of the Assumption of Mary is an occasion for Catholics to honor the belief that God assumed the Virgin Mary to heaven. It is a public holiday in some parts of Germany on August 15 each year.","date":{"iso":"2019-08-15","datetime":{"year":2019,"month":8,"day":15}},"type":["Christian","Common local holiday"],"locations":"BY, SL","states":[{"id":167,"abbrev":"BY","name":"Bavaria","exception":null,"iso":"de-by"},{"id":177,"abbrev":"SL","name":"Saarland","exception":null,"iso":"de-sl"}]},{"name":"All Saints' Day","description":"All Saints' Day is an annual public holiday in five German states to remember all Christian saints on November 1.","date":{"iso":"2019-11-01","datetime":{"year":2019,"month":11,"day":1}},"type":["Christian","Common local holiday"],"locations":"BW, BY, NRW, RLP, SL","states":[{"id":166,"abbrev":"BW","name":"Baden-W\u00fcrttemberg","exception":null,"iso":"de-bw"},{"id":167,"abbrev":"BY","name":"Bavaria","exception":null,"iso":"de-by"},{"id":175,"abbrev":"NRW","name":"North Rhine-Westphalia","exception":null,"iso":"de-nw"},{"id":176,"abbrev":"RLP","name":"Rhineland-Palatinate","exception":null,"iso":"de-rp"},{"id":177,"abbrev":"SL","name":"Saarland","exception":null,"iso":"de-sl"}]}]}}
	 */
	public static StringBuffer parse(StringBuffer buf) {
		//JSON handling here: http://theoryapp.com/parse-json-in-java/
		JSONObject content = new JSONObject(buf.toString());
		JSONObject response = content.getJSONObject("response");
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < response.getJSONArray("holidays").length(); i++) {
			JSONObject holiday = response.getJSONArray("holidays").getJSONObject(i);
			
			output.append("Name" + "\t\t\t" + "Beschreibung" + "\t\t\t" + "Datum"+ "\r\n");
			output.append(holiday.getString("name")).append(";").append(" ");
			output.append(holiday.getString("description")).append(";").append(" ");
			output.append(holiday.getJSONObject("date").getString("iso")).append("\r\n");
			
		}
		return output;
	}
}
