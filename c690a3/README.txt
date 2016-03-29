README.txt
c690 assignment 3

------------
Requirements
------------
Linux, JAVA 1.8, Apache ANT, Apache JENA

-------
Unpack
-------
Unzip the package
- run "tar -zxvf Solver.tar.gz" to unpack the file.
 
-------
Compile
-------
Using ANT:
- from a terminal, go into the "c690a3" directory you just unpacked.
- run "ant clean" to remove any previous compiled files.
- run "ant build" to compile new set of class files.

-------
Execute
-------
Using ANT:
- from a terminal, go into the "c690a3" directory.
- run "ant Solver" to start execute the extraction application.

-------
Output
-------
- The solutions to the questions are stored in 5 tsv files, and a supporting output file for analysis:
    - "cmput690w16a3_q1_cheung.tsv" (Which stadiums are used by which clubs?)
        Format: [Stadium URI] [Stadium Name] [Team URI] [Team Name]
        
    - "cmput690w16a3_q2_cheung.tsv" (Who plays for which teams?)
        Format: [Player URI] [Player Name] [Team URI] [Team Name]
        
    - "cmput690w16a3_q3_cheung.tsv" (Who coaches a team with Spanish players?)
        Format: [Manager URI]
        
    - "cmput690w16a3_q4_cheung.tsv" (Which clubs have stadiums named after former presidents?)
        Format: [Team URI] [Team Name] [Stadium URI] [Stadium Name] [President Name]
    
    - "cmput690w16a3_q5_cheung.tsv" (Which teams have the most nationalities amongst their roaster?)
        Format: [Team URI] [Team Name] [Number Of Nationalities]
    
    - "rdf_output.tsv" (Resulting output of merged graph)
        Format: [Resource URI] [Property NameSpace] [Property LocalName] [Object URI/Literal]

