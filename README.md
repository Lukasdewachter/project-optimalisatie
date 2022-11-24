# Project Optimalisatietechnieken - OMP
## TODO
### Simulated annealing algoritme
1. start solution s0 en start temperature t0
2. t*=bt afkoeling
3. Itereren door solutions totdat eindtemp bereikt wordt
4. Nieuwe oplossing genereren door bv swappen van 2 jobs
4.1 Als het beter is -> accepteer, als slechter -> genereer random tussen 0 en 1 en accepteer als onder t
### Toepassen op first solution
1. Swappen van 2 random buren
2. Eerst random index1 en index1 + 1 genereren en deze swappen of verwijderen
3. Alle jobs in de solution na deze index1 verwijderen, inclusief setuptijd enzo
4. Opnieuw beginnen met toevoegen van jobs startende van index i

### Uitbreiding als dit gelukt is
1. Een random getal tussen 1 en 5 genereren en dan 
naargeling dit getal een case kiezen:
1.1 jobs sorteren volgens grootste penalty rejection
1.2 jobs sorteren volgens duedate
1.3 jobs sorteren volgens earliness penalty
1.4 jobs sorteren volgens release date
1.5 jobs sorteren volgens gemiddelde setuptijd