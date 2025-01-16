[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.sublimis.subsample/subsample-lib)](https://central.sonatype.com/artifact/io.github.sublimis.subsample/subsample-lib)

# SubSampleLib for Java

Fast and simple "Min/max" sub-sampling of a (possibly segmented) list coercible to a two-dimensional (*X/Y*) chart of `double`-s.

Create an instance of this class by passing your [`List<List<T>>`](https://developer.android.com/reference/java/util/List) and a [
`ILSTransformer<T>`](https://github.com/Sublimis/SubSampleLib/blob/main/app/src/main/java/io/github/sublimis/subsample/ILSTransformer.java) of your type
`T` to the constructor.

`Null` elements are not permitted.

Input list(s) are assumed to be sorted by *X*-values in ascending order!

## Results

Original list had about 10 000 points. Chart width is about 1000 pixels.

### Bucket size = 10

10 points per bucket, thus the subsampled list is about 10x smaller.
The sublist will contain about 1000 points, and is well adapted to the chart width, i.e. the pixel density of the screen.
More points would be redundant.

![listsubsample_1000_points.png](https://github.com/Sublimis/SubSampleLib/blob/main/images/listsubsample_1000_points.png)

### Bucket size = 100

100 points per bucket, thus the subsampled list is about 100x smaller.
The sublist will contain about 100 points.

![listsubsample_100_points.png](https://github.com/Sublimis/SubSampleLib/blob/main/images/listsubsample_100_points.png)

### Bucket size = 1000

1000 points per bucket, thus the subsampled list is about 1000x smaller.
The sublist will contain only about 10 points.

![listsubsample_10_points.png](https://github.com/Sublimis/SubSampleLib/blob/main/images/listsubsample_10_points.png)

## About the algorithm

Complexity of the algorithm is just **O(n)**.

Going through the input data, the elements are divided into buckets by X-value.
For each bucket, the minimum and maximum elements by Y-value are found and added to the output in their original X-order.

Two input segments will be merged in the output unless they are separated by more than the bucket size.

## To use this as a library in your app

Add the library as a dependency, e.g. in your `build.gradle` file:

```groovy
implementation 'io.github.sublimis.subsample:subsample-lib:3.1'
```

Make sure that `mavenCentral()` is included in your repositories list.

## Example

```java
final List<List<T>> inputData = ...;    // Your data with elements of type T

final double bucketSize = 10;           // Adjust the bucket size according to your needs

final List<List<T>> sample = new ListSubsample<T>(inputData, new ILSTransformer<T>()
{
	@Override
	public double getX(final T entry)
	{
		return entry.getX();
	}

	@Override
	public double getY(final T entry)
	{
		return entry.getY();
	}
}).getSubsample(bucketSize);
```
