"use strict";
var JParseCodeCli = require("./jparse-code-cli");
/** Simple test for jparser-tools-cli
 * @author TeamworkGuy2
 * @since 2016-06-21
 */
var JParseCodeCliTest;
(function (JParseCodeCliTest) {
    function testJsCli() {
        var opts = JParseCodeCli.createOptions({
            sources: [
                { path: "src/services", depth: 1, fileExtensions: ["java"] },
                { path: "src/models", depth: 2, fileExtensions: ["java"] },
            ],
            destinations: [
                { path: "../models/service-defs.json", namespaces: ["App.Services"] },
                { path: "../models/model-defs.json", namespaces: ["App.Models", "App.Core"] },
            ],
            log: "../output/jparser-tools.log",
            threads: 5,
            debug: true
        });
        var optsStr = JParseCodeCli.stringifyOptions(opts, '"');
        var expStr = ' -sources "' + "src/services=1,[java]" + ";" + "src/models=2,[java]" + '"' +
            ' -destinations "' + "../models/service-defs.json=[App.Services]" + ";" + "../models/model-defs.json=[App.Models,App.Core]" + '"' +
            ' -log "' + "../output/jparser-tools.log" + '"' +
            ' -threads ' + 5 +
            ' -debug';
        if (expStr !== optsStr) {
            throw new Error("options string mismatch, expected: \n'" + expStr + "'\nactual: \n'" + optsStr + "'");
        }
        else {
            console.log("success 'testJsCli()'");
        }
    }
    JParseCodeCliTest.testJsCli = testJsCli;
})(JParseCodeCliTest || (JParseCodeCliTest = {}));
function main() {
    JParseCodeCliTest.testJsCli();
}
main();
module.exports = JParseCodeCliTest;
