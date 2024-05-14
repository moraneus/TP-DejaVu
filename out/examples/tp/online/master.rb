require 'java'
require_relative 'output/TraceMonitor.jar'

# Import the necessary Java classes
java_import 'TraceMonitor'

# Define the file path
file_path = "log.csv"

TraceMonitor.config("20", "", "false", "true") # bits=integer, mode=[debug, profile, ] (not tested), printStat=bool, timedEnabled=bool
sleep(5)

i = 0
# Open the file and read line by line
File.open(file_path, "r") do |file|
  file.each_line do |line|
    # Call the Java method for each line

    result = TraceMonitor.eval(line + "," + i.to_s ) # chomp removes the newline character
    puts result
    i+=1
  end
end