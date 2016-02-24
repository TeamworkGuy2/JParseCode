using System.ServiceModel;
using System.ServiceModel.Web;
using ParserExamples.Models;
using ParserExamples.Searching;

namespace ParserExamples.Services
{
    /// <summary>
    /// This interface provides the contract for track searching.
    /// </summary>
    /// <remarks>
    /// Implementations are expected to be effectively thread-safe.
    /// </remarks>
    [ServiceContract]
    public interface ITrackSearchService
    {
        /// <summary>
        /// Searches tracks.
        /// </summary>
        /// <param name="criteria">The search criteria</param>
        /// <returns>The search result</returns>
        [OperationContract]
        [WebInvoke(Method = "POST", UriTemplate = "/TrackSearch",
            RequestFormat = WebMessageFormat.Json, ResponseFormat = WebMessageFormat.Json)]
        [TransactionFlow(TransactionFlowOption.Allowed)]
        SearchResult<TrackInfo> Search(TrackSearchCriteria criteria) { ; }

        /// <summary>
        /// Searches tracks that have past due date.
        /// </summary>
        /// <param name="albumName">The album name</param>
        /// <returns>The search result</returns>
        [OperationContract]
        [WebInvoke(Method = "POST", UriTemplate = "/GetAlbumTracks?albumName={albumName}",
            ResponseFormat = WebMessageFormat.Json)]
        [TransactionFlow(TransactionFlowOption.Allowed)]
        SearchResult<IDictionary<AlbumInfo, IList<TrackInfo>>> GetAlbumTracks(string albumName) {
            content of block;
        }
    }
}
