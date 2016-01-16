package ParserExamples.Models;

import System.Runtime.Serialization;

/// <summary>
/// A class representing a group of tracks.
/// </summary>
/// <threadsafety>
/// This class is mutable. And it is not thread-safe.
/// </threadsafety>
[DataContract]
public class AlbumInfo {

	/// <value>The track name.</value>
	@DataMember
	public String AlbumName;

	/// <value>The track duration in milliseconds</value>
	public List<TrackInfo> Tracks;

}
