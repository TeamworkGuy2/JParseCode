
/** Generate CLI strings for jparse-code.jar from javascript objects
 * @author TeamworkGuy2
 * @since 2016-06-21
 */
module JParseCodeCli {

    export interface ParseOptions {
        sources: { path: string; depth: number; fileExtensions: string[]; }[];
        destinations: { path: string; namespaces: string[]; }[];
        log?: string | null;
        threads?: number | null;
        debug?: boolean | null;
    }


    export function createOptions(opts: ParseOptions) {
        return opts;
    }


    export function stringifyOptions(opts: ParseOptions, quoteChar = '"') {
        var srcs = opts.sources;
        var dsts = opts.destinations;

        return " -sources " + quoteChar + srcs.map(s => s.path + "=" + s.depth + "," + "[" + s.fileExtensions.join(",") + "]").join(";") + quoteChar +
            " -destinations " + quoteChar + dsts.map(d => d.path + "=" + "[" + d.namespaces.join(",") + "]").join(";") + quoteChar +
            (opts.log ? " -log " + quoteChar + opts.log + quoteChar : "") +
            (!isNaN(opts.threads) ? " -threads " + opts.threads : "") +
            (opts.debug === true ? " -debug" : "");
    }

}

export = JParseCodeCli;