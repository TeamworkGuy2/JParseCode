using System.Runtime.Serialization;

namespace ParserExamples.Models {

    /// <summary>
    /// A class representing a Track.
    /// </summary>
    /// <threadsafety>
    /// This class is mutable. And it is not thread-safe.
    /// </threadsafety>
    [DataContract]
    public class TrackInfo {

        /// <value>The track name.</value>
        [DataMember]
        public string Name { get; set; }

        /// <value>The artist/band name.</value>
        [DataMember]
        public string artist { get; set; }

        /// <value>The track duration in milliseconds</value>
        public int durationMillis { get; set }

        /// <value>The track duration in milliseconds</value>
        public long contentId;

    }

}
