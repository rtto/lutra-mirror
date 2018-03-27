echo "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." > generated-test.ttl
echo "@prefix owl: <http://www.w3.org/2002/07/owl#> ." >> generated-test.ttl
echo "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." >> generated-test.ttl
echo "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." >> generated-test.ttl
echo "@prefix ex: <http://example.org#> ." >> generated-test.ttl
echo "@prefix ottr: <http://ns.ottr.xyz/templates#> ." >> generated-test.ttl

for i in {0..10}
  do 
      echo "[] ottr:templateRef <http://candidate.ottr.xyz/rdfs/ResourceDescription> ;"  >> generated-test.ttl
      echo "ottr:withValues ( ex:test$i \"label$i\" \"comment$i\" \"seeAlso$i\" \"def$i\" ) ."  >> generated-test.ttl
 done
