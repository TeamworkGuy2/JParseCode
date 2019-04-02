"use strict";
/** Generate CLI strings for jparse-code.jar from javascript objects
 * @author TeamworkGuy2
 * @since 2016-06-21
 */
var JParseCodeCli;
(function (JParseCodeCli) {
    function createOptions(opts) {
        return opts;
    }
    JParseCodeCli.createOptions = createOptions;
    function stringifyOptions(opts, quoteChar) {
        if (quoteChar === void 0) { quoteChar = '"'; }
        var srcs = opts.sources;
        var dsts = opts.destinations;
        return " -sources " + quoteChar + srcs.map(function (s) { return s.path + "=" + s.depth + "," + "[" + s.fileExtensions.join(",") + "]"; }).join(";") + quoteChar +
            " -destinations " + quoteChar + dsts.map(function (d) { return d.path + "=" + "[" + d.namespaces.join(",") + "]"; }).join(";") + quoteChar +
            (opts.log ? " -log " + quoteChar + opts.log + quoteChar : "") +
            (!isNaN(opts.threads) ? " -threads " + opts.threads : "") +
            (opts.debug === true ? " -debug" : "");
    }
    JParseCodeCli.stringifyOptions = stringifyOptions;
})(JParseCodeCli || (JParseCodeCli = {}));
module.exports = JParseCodeCli;
