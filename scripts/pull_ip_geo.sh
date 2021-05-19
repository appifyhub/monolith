#!/bin/bash

DESTINATION="src/main/resources/ip2location"

if [ -z "$IP_2_LOCATION_TOKEN" ]; then
  echo "'IP_2_LOCATION_TOKEN' is not set, unable to proceed."
  exit 1
fi

echo ""
echo "Pulling the IP2Location database..."
echo ""

curl --request GET -L \
     --url "https://www.ip2location.com/download/?token=${IP_2_LOCATION_TOKEN}&file=DB3LITEBINIPV6" \
     --output "$DESTINATION/IP2Location.zip"

echo "Unzipping downloaded archive..."
echo ""
unzip -o "$DESTINATION/IP2Location.zip" -d "$DESTINATION"

echo ""
echo "Removing unnecessary files..."
echo ""
rm "$DESTINATION/README_LITE.TXT"
rm "$DESTINATION/IP2Location.zip"
mv "$DESTINATION/IP2LOCATION-LITE-DB3.IPV6.BIN" "$DESTINATION/IP2Location.bin"

echo "Done."
