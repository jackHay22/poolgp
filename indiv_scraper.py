import sys, json, argparse

parser = argparse.ArgumentParser(description='Run scraper for individuals')

parser.add_argument('--input', '-i',
                    dest='input_filename',
                    help='Specify a Clojush log output file',
                    required=True)
parser.add_argument('--output', '-o',
                    dest='output_filename',
                    help='Specify a json output filename for tournament setup',
                    required=True)
parser.add_argument('--fitness', '-f',
                    dest='max_fit',
                    default=100, type=int,
                    help='Specify a fitness required to include an individual')

args = parser.parse_args()

engine_log = open(args.input_filename,'r')
json_out = open(args.output_filename,'w')

all_lines = engine_log.readlines()

tournament_map = {"entrants" : []}

for l in range(len(all_lines)):
    if all_lines[l].startswith('Best program:'):
        error = int(all_lines[l+2].split(" ")[1])

        if error < args.max_fit:
            #14 removes "Best Program:"
            program = all_lines[l][14:].rstrip()
            tournament_map["entrants"].append({"id" : error,
                                               "strategy" : program})

#write dict
json.dump(tournament_map, json_out)

engine_log.close()
json_out.close()
