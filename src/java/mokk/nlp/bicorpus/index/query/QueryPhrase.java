/*
 * Created by Peter Halacsy <peter at halacsy.com>
 * 
 * This work is licensed under the Creative Commons 
 * Attribution License. To view a copy of this license, 
 * visit http://creativecommons.org/licenses/by/2.0/ 
 * or send a letter to Creative Commons, 559 Nathan Abbott Way, 
 * Stanford, California 94305, USA.
 * 
 * Created on Jul 1, 2005
 *
 */

package mokk.nlp.bicorpus.index.query;

public class QueryPhrase {

	public static final class Qualifier {
		public static final Qualifier SHOULD = new Qualifier();
		public static final Qualifier MUST = new Qualifier();
		public static final Qualifier MUSTNOT = new Qualifier();

	};

	public static final class Field {
		public static final Field LEFT = new Field();
		public static final Field RIGHT = new Field();
	};

	public Qualifier qualifier;
	public boolean stemmed;
	public Field field;

	String[] terms;

	/**
	 * @param field2
	 * @param terms2
	 * @param qualifier2
	 * @param stemmed2
	 */
	public QueryPhrase(Field field, String[] terms, Qualifier qualifier,
			boolean stemmed) {
		this.field = field;
		this.terms = terms;
		this.qualifier = qualifier;
		this.stemmed = stemmed;

	}

	public String[] getTerms() {
		return terms;
	}

	public boolean isStemmed() {
		return stemmed;
	}

	public Qualifier getQualifier() {
		return qualifier;
	}

	public Field getField() {
		return field;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Phrase:");
		for (int i = 0; i < terms.length; ++i) {
			buff.append(" ");
			buff.append(terms[i]);
		}
		buff.append(" ");
		buff.append(qualifier == Qualifier.SHOULD ? "SHOULD"
				: (qualifier == Qualifier.MUST ? "MUST" : "MUSTNOT"));
		buff.append(" ");
		buff.append(field == Field.LEFT ? "LEFT" : "RIGHT");
		buff.append(" ");

		buff.append(stemmed ? "stemmed" : "not stemmed");
		buff.append('\n');
		return buff.toString();

	}

}