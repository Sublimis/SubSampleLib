/*
 * Copyright 2025 Sublimis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.sublimis.subsample;


/**
 * @author Sublimis
 * @version 3.1 (2025-01-16)
 */
public class LSPair<F, S>
{
	public final F first;
	public final S second;

	public LSPair(F first, S second)
	{
		this.first = first;
		this.second = second;
	}

	public static <F, S> LSPair<F, S> create(F first, S second)
	{
		return new LSPair<>(first, second);
	}
}
