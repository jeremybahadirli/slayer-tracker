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
package com.slayertracker.groups;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import net.runelite.api.NPC;

public final class Variant
{
	private static final int DEFAULT_SLAYER_XP = -1;

	@Getter
	private final String id;
	@Getter
	private final String name;
	private final int slayerXp;
	private final Match[] matchers;

	private Variant(String id, String name, int slayerXp, Match... matches)
	{
		this.id = id;
		this.name = name;
		this.slayerXp = slayerXp;
		this.matchers = matches == null ? new Match[0] : Arrays.copyOf(matches, matches.length);
	}

	public static Variant of(String id, String name, Match... matches)
	{
		return new Variant(id, name, DEFAULT_SLAYER_XP, matches);
	}

	public static Variant of(String id, String name, int slayerXp, Match... matches)
	{
		return new Variant(id, name, slayerXp, matches);
	}

	public static Variant of(String name, Match... matches)
	{
		return new Variant(name, name, DEFAULT_SLAYER_XP, matches);
	}

	boolean matches(NPC npc)
	{
		if (matchers.length == 0)
		{
			return false;
		}

		return Arrays.stream(matchers).anyMatch(match -> match.matches(npc));
	}

	public Optional<Integer> getSlayerXp()
	{
		return slayerXp > 0 ? Optional.of(slayerXp) : Optional.empty();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof Variant))
		{
			return false;
		}
		Variant variant = (Variant) o;
		return id.equalsIgnoreCase(variant.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id.toLowerCase());
	}

	@Override
	public String toString()
	{
		return id;
	}
}
