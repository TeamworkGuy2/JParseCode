package ParserExamples.Models;

import java.io.Serializable;

import System.Runtime.Serialization;

/// <summary>
/// A class representing a Track.
/// </summary>
/// <threadsafety>
/// This class is mutable. And it is not thread-safe.
/// </threadsafety>
@DataContract
public class TrackInfo extends BaseClass implements Serializable, Comparable<TrackInfo> {

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


	@Override
	public int compareTo(TrackInfo o) {
		return other != null ? (this.Name != null ? this.Name.CompareTo(other.Name) : (other.Name != null ? 1 : 0)) : (this.Name != null ? -1 : 0);
	}


	public <TType extends Number> TType refresh(TType tt) {
		return tt;
	}


	class ArtistMeta {
		public String Name;
		Guid guid;
		protected int referenceCount;
	}

}
