package com.emmanuelmess.simpleaccounting.utils;

import java.util.ArrayList;
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

}
