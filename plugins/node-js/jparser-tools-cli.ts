
/** Generate CLI strings for jparser-tools.jar from javascript objects
 * @author TeamworkGuy2
 * @since 2016-06-21
 */
module JParserToolsCli {

    export interface Options {
        sources: { path: string; depth: number; fileExtensions: string[]; }[];
        destinations: { path: string; namespaces: string[]; }[];
        log: string;
    }


    export function createOptions(opts: Options) {
        return opts;
    }


    export function stringifyOptions(opts: Options, quoteChar = '"') {
        var srcs = opts.sources;
        var dsts = opts.destinations;

        return " -sources " + quoteChar + srcs.map(s => s.path + "=" + s.depth + "," + "[" + s.fileExtensions.join(",") + "]").join(";") + quoteChar +
            " -destinations " + quoteChar + dsts.map(d => d.path + "=" + "[" + d.namespaces.join(",") + "]").join(";") + quoteChar +
            (opts.log ? " -log " + quoteChar + opts.log + quoteChar : "");
    }

}

export = JParserToolsCli;