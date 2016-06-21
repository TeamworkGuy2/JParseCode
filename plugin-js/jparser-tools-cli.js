"use strict";
/** Generate CLI strings for jparser-tools.jar from javascript objects
 * @author TeamworkGuy2
 * @since 2016-06-21
 */
var JParserToolsCli;
(function (JParserToolsCli) {
    function createOptions(opts) {
        return opts;
    }
    JParserToolsCli.createOptions = createOptions;
    function stringifyOptions(opts, quoteChar) {
        if (quoteChar === void 0) { quoteChar = '"'; }
        var srcs = opts.sources;
        var dsts = opts.destinations;
        return " -sources " + quoteChar + srcs.map(function (s) { return s.path + "=" + s.depth + "," + "[" + s.fileExtensions.join(",") + "]"; }).join(";") + quoteChar +
            " -destinations " + quoteChar + dsts.map(function (d) { return d.path + "=" + "[" + d.namespaces.join(",") + "]"; }).join(";") + quoteChar +
            (opts.log ? " -log " + quoteChar + opts.log + quoteChar : "");
    }
    JParserToolsCli.stringifyOptions = stringifyOptions;
})(JParserToolsCli || (JParserToolsCli = {}));
module.exports = JParserToolsCli;
