package conquest.utils;

import java.util.HashMap;

public class HashMapInt<KEY> extends HashMap<KEY, Integer> {

	/**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = 7133255349129855100L;

	@Override
	public Integer get(Object key) {
		Integer result = super.get(key);
		if (result == null) return 0;
		return result;
	}
	
	public Integer inc(KEY key) {
		return put(key, get(key)+1);
	}
	
	public Integer dec(KEY key) {
		return put(key, get(key)-1);
	}
	
	public Integer inc(KEY key, int number) {
		return put(key, get(key)+number);
	}
	
	public Integer dec(KEY key, int number) {
		return put(key, get(key)-number);
	}
	
}
