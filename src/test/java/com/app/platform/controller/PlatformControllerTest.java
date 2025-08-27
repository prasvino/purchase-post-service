package com.app.platform.controller;

import com.app.platform.entity.Platform;
import com.app.platform.repo.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformControllerTest {

    @Mock
    private PlatformRepository platformRepository;

    @InjectMocks
    private PlatformController platformController;

    private List<Platform> samplePlatforms;

    @BeforeEach
    void setUp() {
        samplePlatforms = Arrays.asList(
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Amazon")
                    .domain("amazon.com")
                    .logoUrl("https://amazon.com/favicon.ico")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("eBay")
                    .domain("ebay.com")
                    .logoUrl("https://ebay.com/favicon.ico")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Etsy")
                    .domain("etsy.com")
                    .logoUrl("https://etsy.com/favicon.ico")
                    .verified(true)
                    .build()
        );
    }

    @Test
    void getPlatforms_PlatformsExist_ReturnsExistingPlatforms() {
        // Given
        when(platformRepository.count()).thenReturn(3L);
        when(platformRepository.findAll()).thenReturn(samplePlatforms);

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Amazon");
        assertThat(response.getBody().get(1).getName()).isEqualTo("eBay");
        assertThat(response.getBody().get(2).getName()).isEqualTo("Etsy");

        // Verify that sample platforms were not created since platforms already exist
        verify(platformRepository).count();
        verify(platformRepository).findAll();
        verify(platformRepository, never()).saveAll(anyList());
    }

    @Test
    void getPlatforms_NoPlatformsExist_CreatesSamplePlatformsAndReturns() {
        // Given
        when(platformRepository.count()).thenReturn(0L);
        when(platformRepository.findAll()).thenReturn(samplePlatforms);
        when(platformRepository.saveAll(anyList())).thenReturn(samplePlatforms);

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);

        // Verify that sample platforms were created
        verify(platformRepository).count();
        verify(platformRepository).saveAll(anyList());
        verify(platformRepository).findAll();
    }

    @Test
    void getPlatforms_NoPlatformsExist_CreatesSamplePlatformsWithCorrectData() {
        // Given
        when(platformRepository.count()).thenReturn(0L);
        when(platformRepository.findAll()).thenReturn(samplePlatforms);

        ArgumentCaptor<List<Platform>> platformsCaptor = ArgumentCaptor.forClass(List.class);
        when(platformRepository.saveAll(platformsCaptor.capture())).thenReturn(samplePlatforms);

        // When
        platformController.getPlatforms();

        // Then
        List<Platform> capturedPlatforms = platformsCaptor.getValue();
        assertThat(capturedPlatforms).hasSize(5);

        // Verify Amazon platform
        Platform amazon = capturedPlatforms.stream()
                .filter(p -> "Amazon".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertThat(amazon).isNotNull();
        assertThat(amazon.getDomain()).isEqualTo("amazon.com");
        assertThat(amazon.getLogoUrl()).isEqualTo("https://amazon.com/favicon.ico");
        assertThat(amazon.isVerified()).isTrue();
        assertThat(amazon.getId()).isNotNull();

        // Verify eBay platform
        Platform ebay = capturedPlatforms.stream()
                .filter(p -> "eBay".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertThat(ebay).isNotNull();
        assertThat(ebay.getDomain()).isEqualTo("ebay.com");
        assertThat(ebay.getLogoUrl()).isEqualTo("https://ebay.com/favicon.ico");
        assertThat(ebay.isVerified()).isTrue();

        // Verify Etsy platform
        Platform etsy = capturedPlatforms.stream()
                .filter(p -> "Etsy".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertThat(etsy).isNotNull();
        assertThat(etsy.getDomain()).isEqualTo("etsy.com");
        assertThat(etsy.getLogoUrl()).isEqualTo("https://etsy.com/favicon.ico");
        assertThat(etsy.isVerified()).isTrue();

        // Verify Best Buy platform
        Platform bestBuy = capturedPlatforms.stream()
                .filter(p -> "Best Buy".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertThat(bestBuy).isNotNull();
        assertThat(bestBuy.getDomain()).isEqualTo("bestbuy.com");
        assertThat(bestBuy.getLogoUrl()).isEqualTo("https://bestbuy.com/favicon.ico");
        assertThat(bestBuy.isVerified()).isTrue();

        // Verify Target platform
        Platform target = capturedPlatforms.stream()
                .filter(p -> "Target".equals(p.getName()))
                .findFirst()
                .orElse(null);
        assertThat(target).isNotNull();
        assertThat(target.getDomain()).isEqualTo("target.com");
        assertThat(target.getLogoUrl()).isEqualTo("https://target.com/favicon.ico");
        assertThat(target.isVerified()).isTrue();
    }

    @Test
    void getPlatforms_EmptyPlatformsList_ReturnsEmptyList() {
        // Given
        when(platformRepository.count()).thenReturn(1L); // Platforms exist
        when(platformRepository.findAll()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();

        // Verify that sample platforms were not created since count > 0
        verify(platformRepository).count();
        verify(platformRepository).findAll();
        verify(platformRepository, never()).saveAll(anyList());
    }

    @Test
    void getPlatforms_SinglePlatform_ReturnsSinglePlatform() {
        // Given
        Platform singlePlatform = Platform.builder()
                .id(UUID.randomUUID())
                .name("Custom Platform")
                .domain("custom.com")
                .logoUrl("https://custom.com/logo.png")
                .verified(false)
                .build();

        when(platformRepository.count()).thenReturn(1L);
        when(platformRepository.findAll()).thenReturn(Arrays.asList(singlePlatform));

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Custom Platform");
        assertThat(response.getBody().get(0).getDomain()).isEqualTo("custom.com");
        assertThat(response.getBody().get(0).isVerified()).isFalse();
    }

    @Test
    void getPlatforms_ManyPlatforms_ReturnsAllPlatforms() {
        // Given
        List<Platform> manyPlatforms = Arrays.asList(
            Platform.builder().id(UUID.randomUUID()).name("Platform 1").domain("platform1.com").verified(true).build(),
            Platform.builder().id(UUID.randomUUID()).name("Platform 2").domain("platform2.com").verified(true).build(),
            Platform.builder().id(UUID.randomUUID()).name("Platform 3").domain("platform3.com").verified(false).build(),
            Platform.builder().id(UUID.randomUUID()).name("Platform 4").domain("platform4.com").verified(true).build(),
            Platform.builder().id(UUID.randomUUID()).name("Platform 5").domain("platform5.com").verified(false).build()
        );

        when(platformRepository.count()).thenReturn(5L);
        when(platformRepository.findAll()).thenReturn(manyPlatforms);

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(5);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Platform 1");
        assertThat(response.getBody().get(4).getName()).isEqualTo("Platform 5");
    }

    @Test
    void getPlatforms_VerifiedAndUnverifiedPlatforms_ReturnsAllPlatforms() {
        // Given
        List<Platform> mixedPlatforms = Arrays.asList(
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Verified Platform")
                    .domain("verified.com")
                    .logoUrl("https://verified.com/logo.png")
                    .verified(true)
                    .build(),
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Unverified Platform")
                    .domain("unverified.com")
                    .logoUrl("https://unverified.com/logo.png")
                    .verified(false)
                    .build()
        );

        when(platformRepository.count()).thenReturn(2L);
        when(platformRepository.findAll()).thenReturn(mixedPlatforms);

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        
        Platform verifiedPlatform = response.getBody().stream()
                .filter(Platform::isVerified)
                .findFirst()
                .orElse(null);
        assertThat(verifiedPlatform).isNotNull();
        assertThat(verifiedPlatform.getName()).isEqualTo("Verified Platform");

        Platform unverifiedPlatform = response.getBody().stream()
                .filter(p -> !p.isVerified())
                .findFirst()
                .orElse(null);
        assertThat(unverifiedPlatform).isNotNull();
        assertThat(unverifiedPlatform.getName()).isEqualTo("Unverified Platform");
    }

    @Test
    void getPlatforms_PlatformsWithNullLogoUrl_HandlesCorrectly() {
        // Given
        List<Platform> platformsWithNullLogo = Arrays.asList(
            Platform.builder()
                    .id(UUID.randomUUID())
                    .name("Platform Without Logo")
                    .domain("nologo.com")
                    .logoUrl(null)
                    .verified(true)
                    .build()
        );

        when(platformRepository.count()).thenReturn(1L);
        when(platformRepository.findAll()).thenReturn(platformsWithNullLogo);

        // When
        ResponseEntity<List<Platform>> response = platformController.getPlatforms();

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getLogoUrl()).isNull();
        assertThat(response.getBody().get(0).getName()).isEqualTo("Platform Without Logo");
    }

    @Test
    void getPlatforms_SamplePlatformCreation_GeneratesUniqueIds() {
        // Given
        when(platformRepository.count()).thenReturn(0L);
        when(platformRepository.findAll()).thenReturn(samplePlatforms);

        ArgumentCaptor<List<Platform>> platformsCaptor = ArgumentCaptor.forClass(List.class);
        when(platformRepository.saveAll(platformsCaptor.capture())).thenReturn(samplePlatforms);

        // When
        platformController.getPlatforms();

        // Then
        List<Platform> capturedPlatforms = platformsCaptor.getValue();
        
        // Verify all platforms have unique IDs
        long uniqueIdCount = capturedPlatforms.stream()
                .map(Platform::getId)
                .distinct()
                .count();
        assertThat(uniqueIdCount).isEqualTo(capturedPlatforms.size());

        // Verify all IDs are not null
        capturedPlatforms.forEach(platform -> 
            assertThat(platform.getId()).isNotNull()
        );
    }

    @Test
    void getPlatforms_SamplePlatformCreation_AllPlatformsAreVerified() {
        // Given
        when(platformRepository.count()).thenReturn(0L);
        when(platformRepository.findAll()).thenReturn(samplePlatforms);

        ArgumentCaptor<List<Platform>> platformsCaptor = ArgumentCaptor.forClass(List.class);
        when(platformRepository.saveAll(platformsCaptor.capture())).thenReturn(samplePlatforms);

        // When
        platformController.getPlatforms();

        // Then
        List<Platform> capturedPlatforms = platformsCaptor.getValue();
        
        // Verify all sample platforms are verified
        capturedPlatforms.forEach(platform -> 
            assertThat(platform.isVerified()).isTrue()
        );
    }
}