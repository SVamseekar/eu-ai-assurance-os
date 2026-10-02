# Verifying an Assurance OS evidence pack

Every JSON pack carries `contentSha256` (SHA-256 of the canonical pack content) and `signature`, a JWS (RS256)
whose payload binds `contentSha256`, `systemId`, `generatedAt`, `evidencePackVersion`, and `auditChainHead`.
The public keys are published at `https://<your-workspace-host>/.well-known/jwks.json`.

```python
import json, sys, urllib.request
from jose import jws            # pip install python-jose

pack = json.load(open(sys.argv[1]))
jwks = json.load(urllib.request.urlopen(sys.argv[2]))
header = jws.get_unverified_header(pack["signature"])
key = next(k for k in jwks["keys"] if k["kid"] == header["kid"])
claims = json.loads(jws.verify(pack["signature"], key, algorithms=["RS256"]))
assert claims["contentSha256"] == pack["contentSha256"], "hash mismatch"
print("signature valid for system", claims["systemId"], "generated", claims["generatedAt"])
```

Recomputing `contentSha256` yourself proves the pack was not edited after export; the signature proves Assurance OS
produced it. Neither is a legal certification.
