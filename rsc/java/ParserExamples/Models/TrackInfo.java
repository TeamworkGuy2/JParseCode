package ParserExamples.Models;

import System.Runtime.Serialization;

/// <summary>
/// A class representing a Track.
/// </summary>
/// <threadsafety>
/// This class is mutable. And it is not thread-safe.
/// </threadsafety>
[DataContract]
public class TrackInfo {

	/// <value>The track name.</value>
	@DataMember
	public String Name;

	/// <value>The artist/band name.</value>
	@DataMember
	public String artist;

	/// <value>The track duration in milliseconds</value>
	public int durationMillis;

	/// <value>The track duration in milliseconds</value>
	public long contentId;

}
