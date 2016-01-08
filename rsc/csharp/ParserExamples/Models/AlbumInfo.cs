using System.Runtime.Serialization;

namespace ParserExamples.Models {

    /// <summary>
    /// A class representing a group of tracks.
    /// </summary>
    /// <threadsafety>
    /// This class is mutable. And it is not thread-safe.
    /// </threadsafety>
    [DataContract]
    public class AlbumInfo {

        /// <value>The track name.</value>
        [DataMember]
        public string AlbumName { get; set; }

        /// <value>The track duration in milliseconds</value>
        public IList<TrackInfo> Tracks { get; set }

    }

}
