/*
 * Copyright (c) 2022, Jeremy Bahadirli <https://github.com/jeremybahadirli>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.slayertracker.records;

import com.slayertracker.groups.Group;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecordMap<G extends Group, R extends Record> extends HashMap<G, R>
{
	private final PropertyChangeSupport support = new PropertyChangeSupport(this);

	public RecordMap(PropertyChangeListener pcl)
	{
		super();
		support.addPropertyChangeListener(pcl);
	}

	@Override
	public R put(G group, R record)
	{
		R r = super.put(group, record);
		support.firePropertyChange("RecordMap " + group.getName(), null, group);
		return r;
	}

	@Override
	public void putAll(Map<? extends G, ? extends R> m)
	{
		super.putAll(m);
		support.firePropertyChange("RecordMap putAll", false, true);
	}

	@Override
	public R remove(Object group)
	{
		R r = super.remove(group);
		support.firePropertyChange("RecordMap remove", group, null);
		return r;
	}

	@Override
	public void clear()
	{
		super.clear();
		support.firePropertyChange("RecordMap clear", this.keySet(), Collections.EMPTY_SET);
	}
}