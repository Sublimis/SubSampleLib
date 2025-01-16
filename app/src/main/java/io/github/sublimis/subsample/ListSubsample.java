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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Fast and simple "Min/max" sub-sampling of a (possibly segmented) list coercible to a two-dimensional (X/Y) chart of <code>double</code>-s.
 * <p>
 * Create an instance of this class by passing your {@link List}&lt;{@link List}&lt;T&gt;&gt; and a {@link ILSTransformer}&lt;T&gt; of your type {@code T} to the constructor.
 * <p>
 * {@code Null} elements are not permitted.
 * <p>
 * Input list(s) are assumed to be sorted by X-values in ascending order!
 * <p>
 * <p>
 * <b>About the algorithm</b>
 * <p>
 * Complexity of the algorithm is just <b>O(n)</b>.
 * <p>
 * Going through the input data, the elements are divided into buckets by X-value.
 * For each bucket, the minimum and maximum elements by Y-value are found and added to the output in their original X-order.
 * <p>
 * Two input segments will be merged in the output unless they are separated by more than the bucket size.
 *
 * @author Sublimis
 * @version 3.1 (2025-01-16)
 */
public class ListSubsample<T>
{
	protected final List<List<T>> mData;

	protected final ILSTransformer<T> mTransformer;

	protected final Comparator<T> mComparatorX = new Comparator<>()
	{
		@Override
		public int compare(final T o1, final T o2)
		{
			return Double.compare(mTransformer.getX(o1), mTransformer.getX(o2));
		}
	};

	protected final Comparator<T> mComparatorY = new Comparator<>()
	{
		@Override
		public int compare(final T o1, final T o2)
		{
			return Double.compare(mTransformer.getY(o1), mTransformer.getY(o2));
		}
	};

	/**
	 * Fast and simple "Min/max" sub-sampling of a (possibly segmented) list coercible to a two-dimensional (X/Y) chart of <code>double</code>-s.
	 * <p>
	 * Create an instance of this class by passing your {@link List}&lt;{@link List}&lt;T&gt;&gt; and a {@link ILSTransformer}&lt;T&gt; of your type {@code T} to the constructor.
	 * <p>
	 * {@code Null} elements are not permitted.
	 * <p>
	 * Input list(s) are assumed to be sorted by X-values in ascending order!
	 * <p>
	 * <p>
	 * <b>About the algorithm</b>
	 * <p>
	 * Complexity of the algorithm is just <b>O(n)</b>.
	 * <p>
	 * Going through the input data, the elements are divided into buckets by X-value.
	 * For each bucket, the minimum and maximum elements by Y-value are found and added to the output in their original X-order.
	 * <p>
	 * Two input segments will be merged in the output unless they are separated by more than the bucket size.
	 */
	public ListSubsample(final List<List<T>> data, final ILSTransformer<T> transformer)
	{
		this.mData = data;
		this.mTransformer = transformer;
	}

	/**
	 * @param bucketSize Size of a bucket by which the input will be divided.
	 *                   Elements from one bucket are going to be collapsed into a single sub-sampled "point" in the output ("point" usually consisting of two elements: min and max of the bucket).
	 *                   <p>
	 *                   Smaller value means more points in the output (finer sub-sampling).
	 *                   Set to 0 or -1 to return all points.
	 *                   <p>
	 *                   If used with a charting library, a good value for this is the ratio: <code>(input window width) / (chart window width)</code>.
	 * @return Appropriately sub-sampled data. Returned data will always have the same first and last points as the original data.
	 */
	public List<List<T>> getSubsample(final double bucketSize)
	{
		final List<List<T>> output = new ArrayList<>();

		final List<List<T>> listList = mData;

		if (LSUtils.isValidAndNotEmpty(listList))
		{
			final T firstFirst = LSUtils.getFirstFirst(listList);

			T min = null, max = null;
			long minIndex = -1, maxIndex = -1;
			T last = null;
			double lastBucket = -1;

			List<T> oneSegment = new ArrayList<>();

			long globalIndex = 0;

			for (final List<T> list : listList)
			{
				for (int index = 0; index < (list == null ? 0 : list.size()); index++, globalIndex++)
				{
					final T current = list.get(index);

					final double bucket = getBucket(current, firstFirst, bucketSize);

					final boolean shouldStartNewSegment = index <= 0 && shouldStartNewSegment(current, last, bucketSize);

					// Constant 1.25 is a heuristic to make the output look better (anything > 1.0 should work)
					if (Math.abs(bucket - lastBucket) > 1.25 || shouldStartNewSegment)
					{
						insertPoint(oneSegment, min, max, minIndex, maxIndex);

						if (shouldStartNewSegment)
						{
							if (LSUtils.isValidAndNotEmpty(oneSegment))
							{
								output.add(oneSegment);

								oneSegment = new ArrayList<>();
							}
						}

						insertPoint(oneSegment, current, current, globalIndex, globalIndex);

						min = max = null;
						minIndex = maxIndex = -1;

						lastBucket = bucket;
						last = current;
						continue;
					}

					if (last == null || lastBucket != bucket || bucket < 0)
					{
						if (last != null)
						{
							insertPoint(oneSegment, min, max, minIndex, maxIndex);
						}

						min = max = current;
						minIndex = maxIndex = globalIndex;
					}

					lastBucket = bucket;
					last = current;

					if (min == null || mComparatorY.compare(current, min) < 0)
					{
						min = current;
						minIndex = globalIndex;
					}

					if (max == null || mComparatorY.compare(current, max) >= 0)
					{
						// Prefer returning the rightmost element as "max" if all elements are equal (leftmost will be "min" in this case)
						max = current;
						maxIndex = globalIndex;
					}
				}
			}

			insertPoint(oneSegment, min, max, minIndex, maxIndex);

			if (LSUtils.isValidAndNotEmpty(oneSegment))
			{
				output.add(oneSegment);
			}

			// We want to make sure the output has the same first and last points as the input (a desired property)
			if (LSUtils.isValidAndNotEmpty(output))
			{
				final LSPair<T, T> inputRange = LSUtils.getRange(listList);
				final LSPair<T, T> outputRange = LSUtils.getRange(output);

				if (inputRange != null && outputRange != null)
				{
					final T inputFirst = inputRange.first;
					final T inputLast = inputRange.second;

					final T outputFirst = outputRange.first;
					final T outputLast = outputRange.second;

					if (outputFirst != inputFirst)
					{
						LSUtils.addFirst(LSUtils.getFirst(output), inputFirst);
					}

					if (outputLast != inputLast)
					{
						LSUtils.addLast(LSUtils.getLast(output), inputLast);
					}
				}
			}
		}

		return output;
	}

	/**
	 * @param pointsCount How many points are required in the output list.
	 * @param range       The range of X-values in the input data. You can use {@link LSUtils#getRange(List)} to get this.
	 * @return Appropriately sub-sampled data. Returned data will always have the same first and last points as the original data.
	 */
	public List<List<T>> getSubsampleByCount(final int pointsCount, final double range)
	{
		final double bucketSize;
		{
			final int ourPointsCount = LSUtils.getPointsCount(mData);

			if (1.5 * pointsCount >= ourPointsCount || pointsCount <= 0)
			{
				bucketSize = 0;
			}
			else
			{
				bucketSize = range / pointsCount;
			}
		}

		return getSubsample(bucketSize);
	}

	protected double getBucket(final T current, final T first, final double bucketSize)
	{
		if (bucketSize <= 0)
		{
			return -1;
		}
		else
		{
			return Math.floor((mTransformer.getX(current) - mTransformer.getX(first)) / bucketSize);
		}
	}

	protected boolean shouldStartNewSegment(final T current, final T last, final double bucketSize)
	{
		boolean retVal = false;

		if (last != null && current != null)
		{
			retVal = mTransformer.getX(current) - mTransformer.getX(last) >= bucketSize;
		}

		return retVal;
	}

	protected void insertPoint(final List<T> oneSegment, final T min, final T max, final long minIndex, final long maxIndex)
	{
		if (min == null || max == null)
		{
			// Do nothing
		}
		else if (min == max)
		{
			oneSegment.add(min);
		}
		else
		{
			final int compare = mComparatorX.compare(min, max);

			if (compare < 0)
			{
				oneSegment.add(min);
				oneSegment.add(max);
			}
			else if (compare > 0)
			{
				oneSegment.add(max);
				oneSegment.add(min);
			}
			else
			{
				if (minIndex <= maxIndex)
				{
					oneSegment.add(min);
					oneSegment.add(max);
				}
				else
				{
					oneSegment.add(max);
					oneSegment.add(min);
				}
			}
		}
	}
}
