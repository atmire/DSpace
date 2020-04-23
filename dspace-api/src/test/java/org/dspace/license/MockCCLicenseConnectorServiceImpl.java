/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.license;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation for the Creative commons license connector service.
 * This class will return a structure of CC Licenses similar to the CC License API but without having to contact it
 */
public class MockCCLicenseConnectorServiceImpl extends CCLicenseConnectorServiceImpl {

    /**
     * Retrieves mock CC Licenses for the provided language
     * @param language - the language
     * @return a map of mocked licenses with the id and the license
     */
    public Map<String, CCLicense> retrieveLicenses(String language) {
        Map<String, CCLicense> ccLicenses = new HashMap<>();
        CCLicense mockLicense1 = createMockLicense(1, new int[]{3, 2, 3});
        CCLicense mockLicense2 = createMockLicense(2, new int[]{2});
        CCLicense mockLicense3 = createMockLicense(3, new int[]{});

        ccLicenses.put(mockLicense1.getLicenseId(), mockLicense1);
        ccLicenses.put(mockLicense2.getLicenseId(), mockLicense2);
        ccLicenses.put(mockLicense3.getLicenseId(), mockLicense3);

        return ccLicenses;
    }

    private CCLicense createMockLicense(int count, int[] amountOfFieldsAndEnums) {
        String licenseId = "license" + count;
        String licenseName = "License " + count + " - Name";
        List<CCLicenseField> mockLicenseFields = createMockLicenseFields(count, amountOfFieldsAndEnums);
        return new CCLicense(licenseId, licenseName, mockLicenseFields);
    }

    private List<CCLicenseField> createMockLicenseFields(int count, int[] amountOfFieldsAndEnums) {
        List<CCLicenseField> ccLicenseFields = new LinkedList<>();
        for (int index = 0; index < amountOfFieldsAndEnums.length; index++) {
            String licenseFieldId = "license" + count + "-field" + index;
            String licenseFieldLabel = "License " + count + " - Field " + index + " - Label";
            String licenseFieldDescription = "License " + count + " - Field " + index + " - Description";
            List<CCLicenseFieldEnum> mockLicenseFields = createMockLicenseFields(count,
                                                                                 index,
                                                                                 amountOfFieldsAndEnums[index]);
            ccLicenseFields.add(new CCLicenseField(licenseFieldId,
                                                   licenseFieldLabel,
                                                   licenseFieldDescription,
                                                   mockLicenseFields));

        }

        return ccLicenseFields;
    }

    private List<CCLicenseFieldEnum> createMockLicenseFields(int count, int index, int amountOfEnums) {
        List<CCLicenseFieldEnum> ccLicenseFieldEnumList = new LinkedList<>();
        for (int i = 0; i < amountOfEnums; i++) {
            String enumId = "license" + count + "-field" + index + "-enum" + i;
            String enumLabel = "License " + count + " - Field " + index + " - Enum " + i + " - Label";
            String enumDescription = "License " + count + " - Field " + index + " - Enum " + i + " - " +
                    "Description";
            ccLicenseFieldEnumList.add(new CCLicenseFieldEnum(enumId, enumLabel, enumDescription));
        }
        return ccLicenseFieldEnumList;

    }

}