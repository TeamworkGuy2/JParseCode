using System;
using System.Runtime.Serialization;

namespace ParserExamples.Models
{
    /// <summary>
    /// A class representing a Track.
    /// </summary>
    /// <threadsafety>
    /// This class is mutable. And it is not thread-safe.
    /// </threadsafety>
    [DataContract]
    public class TrackInfo : BaseClass, ISerializable, IComparable<TrackInfo>
	{
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


        public int CompareTo(Implementer other) {
            return other != null ? (this.Name != null ? this.Name.CompareTo(other.Name) : (other.Name != null ? 1 : 0)) : (this.Name != null ? -1 : 0);
        }


        public void GetObjectData(SerializationInfo info, StreamingContext context) {
            var a = Int32.Parse("");
        }


        public TType Refresh<TType>(TType tt) where TType : IConvertible {
            return tt;
        }
    }
}
