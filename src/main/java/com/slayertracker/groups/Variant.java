/*
 * Copyright (c) 2026, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import lombok.Getter;
import net.runelite.api.NPC;

public final class Variant
{
	private static final int DEFAULT_SLAYER_XP = -1;
	private static final Map<String, Variant> REGISTRY = new HashMap<>();

	@Getter
	private final String id;
	@Getter
	private final String name;
	private final int slayerXp;
	@Getter
	private final Predicate<NPC> npcPredicate;

	private Variant(@Nullable String id, String name, int slayerXp, Predicate<NPC> npcPredicate)
	{
		this.id = id;
		this.name = name;
		this.slayerXp = slayerXp;
		this.npcPredicate = npcPredicate;
	}

	public static Variant of(String name, int slayerXp, Predicate<NPC> npcPredicate)
	{
		return new Variant(null, name, slayerXp, npcPredicate);
	}

	public static Variant of(String name, Predicate<NPC> npcPredicate)
	{
		return Variant.of(name, DEFAULT_SLAYER_XP, npcPredicate);
	}

	public static Variant of(String name)
	{
		return Variant.of(name, NpcPredicates.byName(name));
	}

	static Variant scopeToAssignment(String assignmentKey, Variant template)
	{
		String scopedId = generateScopedId(assignmentKey, template.name);
		Variant scopedVariant = new Variant(scopedId, template.name, template.slayerXp, template.npcPredicate);
		register(scopedVariant);
		return scopedVariant;
	}

	public static Optional<Variant> getById(String id)
	{
		if (id == null)
		{
			return Optional.empty();
		}

		return Optional.ofNullable(REGISTRY.get(id.toUpperCase(Locale.ROOT)));
	}

	private static void register(Variant variant)
	{
		String key = variant.id.toUpperCase(Locale.ROOT);
		Variant existing = REGISTRY.putIfAbsent(key, variant);
		if (existing != null)
		{
			throw new IllegalStateException("Duplicate variant id: " + variant.id);
		}
	}

	private static String generateScopedId(String assignmentKey, String variantName)
	{
		String assignmentSlug = slugify(assignmentKey);
		String variantSlug = slugify(variantName);
		return assignmentSlug + "__" + variantSlug;
	}

	private static String slugify(String value)
	{
		if (value == null)
		{
			return "";
		}

		String slug = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
		slug = slug.replaceAll("^_+", "").replaceAll("_+$", "");
		return slug.isEmpty() ? "variant" : slug;
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
		if (id == null || variant.id == null)
		{
			return false;
		}
		return id.equalsIgnoreCase(variant.id);
	}

	@Override
	public String toString()
	{
		return id;
	}
}
