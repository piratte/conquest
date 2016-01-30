package conquest.utils;

import java.util.HashMap;


public class HashMapMapInt<KEY1, KEY2> extends HashMap<KEY1, HashMapInt<KEY2>> {

	/**
	 * Auto-generated.
	 */
	private static final long serialVersionUID = 7133255349129855100L;

	public HashMapInt<KEY2> get(Object key1) {
		HashMapInt<KEY2> result = super.get(key1);
		if (result == null) {
			result = new HashMapInt<KEY2>();
			put((KEY1)key1, result);
		}
		return result;
	}
	
	public Integer inc(KEY1 key1, KEY2 key2) {
		return get(key1).inc(key2);
	}
	
	public Integer dec(KEY1 key1, KEY2 key2) {
		return get(key1).dec(key2);
	}
	
	public Integer inc(KEY1 key1, KEY2 key2, int number) {
		return get(key1).inc(key2, number);
	}
	
	public Integer dec(KEY1 key1, KEY2 key2, int number) {
		return get(key1).dec(key2, number);
	}
	
}
