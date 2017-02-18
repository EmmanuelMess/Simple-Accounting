package com.emmanuelmess.simpleaccounting.utils;

import java.util.ArrayList;
import java.util.Collections;
/**
 * @author Emmanuel
 *         on 28/1/2017, at 22:19.
 */

public class RangedStructure {

	private ArrayList<Integer> dataStart = new ArrayList<>();
	private ArrayList<Integer> dataEnd = new ArrayList<>();

	public void add(Integer start, Integer end) {
		dataStart.add(start);
		dataEnd.add(end);
	}

	public void swap(int i1, int i2) {
		Collections.swap(dataStart, i1, i2);
		Collections.swap(dataEnd, i1, i2);
	}

	public boolean contains(Integer check) {
		if(dataStart.contains(check) || dataEnd.contains(check))
			return true;

		for(int i = 0; i < dataStart.size(); i++)
			if (dataStart.get(i) < check)
				if(dataEnd.get(i) >= check)
					return true;

		return false;
	}

	public int get(Integer check) {
		if(dataStart.contains(check)) {
			for (int i = 0; i < dataStart.size(); i++)
				if (dataStart.get(i).equals(check))
					return i;
		} else if(dataEnd.contains(check)) {
			for (int i = 0; i < dataEnd.size(); i++)
				if (dataEnd.get(i).equals(check))
					return i;
		}

		for(int i = 0; i < dataStart.size(); i++)
			if (dataStart.get(i) < check)
				if(dataEnd.get(i) >= check)
					return i;

		return -1;
	}

	public void remove(int index) {
		dataStart.remove(index);
		dataEnd.remove(index);
	}

	public int size() {
		return dataStart.size();
	}

	public String toString() {
		String s = "{";
		for(int i = 0; i < dataStart.size(); i++)
			s += "[" + dataStart.get(i) + "-" + dataEnd.get(i) + "], ";

		return s.substring(0, s.lastIndexOf(",")) + "}";
	}

}
