# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
 #noinspection ShrinkerUnresolvedReference
-keep class net.sqlcipher.**{*;}
-keep class com.squareup.leakcanary**{*;}
-keep class com.shockwave.**
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn androidx.appcompat.widget.ListViewCompat
-dontwarn androidx.compose.material.SwipeableState
-dontwarn java.awt.AlphaComposite
-dontwarn java.awt.BasicStroke
-dontwarn java.awt.Canvas
-dontwarn java.awt.Color
-dontwarn java.awt.Component
-dontwarn java.awt.Composite
-dontwarn java.awt.Font
-dontwarn java.awt.FontMetrics
-dontwarn java.awt.GradientPaint
-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Graphics
-dontwarn java.awt.GraphicsConfiguration
-dontwarn java.awt.Image
-dontwarn java.awt.MediaTracker
-dontwarn java.awt.Paint
-dontwarn java.awt.Polygon
-dontwarn java.awt.Rectangle
-dontwarn java.awt.RenderingHints$Key
-dontwarn java.awt.RenderingHints
-dontwarn java.awt.Shape
-dontwarn java.awt.Stroke
-dontwarn java.awt.TexturePaint
-dontwarn java.awt.font.FontRenderContext
-dontwarn java.awt.font.GlyphVector
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.awt.geom.Arc2D$Double
-dontwarn java.awt.geom.Area
-dontwarn java.awt.geom.Ellipse2D$Float
-dontwarn java.awt.geom.Line2D$Double
-dontwarn java.awt.geom.Line2D
-dontwarn java.awt.geom.NoninvertibleTransformException
-dontwarn java.awt.geom.PathIterator
-dontwarn java.awt.geom.Point2D
-dontwarn java.awt.geom.Rectangle2D$Double
-dontwarn java.awt.geom.Rectangle2D$Float
-dontwarn java.awt.geom.Rectangle2D
-dontwarn java.awt.geom.RoundRectangle2D$Double
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.BufferedImageOp
-dontwarn java.awt.image.ColorModel
-dontwarn java.awt.image.ImageObserver
-dontwarn java.awt.image.ImageProducer
-dontwarn java.awt.image.MemoryImageSource
-dontwarn java.awt.image.PixelGrabber
-dontwarn java.awt.image.RenderedImage
-dontwarn java.awt.image.WritableRaster
-dontwarn java.awt.image.renderable.RenderableImage
-dontwarn java.awt.print.PrinterGraphics
-dontwarn java.awt.print.PrinterJob
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn javax.imageio.IIOImage
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.ImageWriteParam
-dontwarn javax.imageio.ImageWriter
-dontwarn javax.imageio.metadata.IIOMetadata
-dontwarn javax.imageio.plugins.jpeg.JPEGImageWriteParam
-dontwarn javax.imageio.stream.ImageOutputStream
-dontwarn javax.xml.crypto.XMLCryptoContext
-dontwarn javax.xml.crypto.dom.DOMCryptoContext
-dontwarn javax.xml.crypto.dom.DOMStructure
-dontwarn javax.xml.crypto.dsig.CanonicalizationMethod
-dontwarn javax.xml.crypto.dsig.DigestMethod
-dontwarn javax.xml.crypto.dsig.Reference
-dontwarn javax.xml.crypto.dsig.SignatureMethod
-dontwarn javax.xml.crypto.dsig.SignedInfo
-dontwarn javax.xml.crypto.dsig.Transform
-dontwarn javax.xml.crypto.dsig.XMLObject
-dontwarn javax.xml.crypto.dsig.XMLSignContext
-dontwarn javax.xml.crypto.dsig.XMLSignature
-dontwarn javax.xml.crypto.dsig.XMLSignatureFactory
-dontwarn javax.xml.crypto.dsig.dom.DOMSignContext
-dontwarn javax.xml.crypto.dsig.keyinfo.KeyInfo
-dontwarn javax.xml.crypto.dsig.keyinfo.KeyInfoFactory
-dontwarn javax.xml.crypto.dsig.keyinfo.KeyValue
-dontwarn javax.xml.crypto.dsig.keyinfo.X509Data
-dontwarn javax.xml.crypto.dsig.spec.C14NMethodParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.DigestMethodParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.TransformParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec
-dontwarn javax.xml.crypto.dsig.spec.XPathType$Filter
-dontwarn javax.xml.crypto.dsig.spec.XPathType
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMReference
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMSignedInfo
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMUtils
-dontwarn org.apache.jcp.xml.dsig.internal.dom.DOMXMLSignature
-dontwarn org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI
-dontwarn org.apache.xml.security.utils.Base64
-dontwarn org.bouncycastle.asn1.ASN1Encodable
-dontwarn org.bouncycastle.asn1.ASN1EncodableVector
-dontwarn org.bouncycastle.asn1.ASN1Enumerated
-dontwarn org.bouncycastle.asn1.ASN1InputStream
-dontwarn org.bouncycastle.asn1.ASN1Integer
-dontwarn org.bouncycastle.asn1.ASN1ObjectIdentifier
-dontwarn org.bouncycastle.asn1.ASN1OctetString
-dontwarn org.bouncycastle.asn1.ASN1OutputStream
-dontwarn org.bouncycastle.asn1.ASN1Primitive
-dontwarn org.bouncycastle.asn1.ASN1Sequence
-dontwarn org.bouncycastle.asn1.ASN1Set
-dontwarn org.bouncycastle.asn1.ASN1String
-dontwarn org.bouncycastle.asn1.ASN1TaggedObject
-dontwarn org.bouncycastle.asn1.DERIA5String
-dontwarn org.bouncycastle.asn1.DERNull
-dontwarn org.bouncycastle.asn1.DEROctetString
-dontwarn org.bouncycastle.asn1.DEROutputStream
-dontwarn org.bouncycastle.asn1.DERSequence
-dontwarn org.bouncycastle.asn1.DERSet
-dontwarn org.bouncycastle.asn1.DERTaggedObject
-dontwarn org.bouncycastle.asn1.DERUTCTime
-dontwarn org.bouncycastle.asn1.cmp.PKIFailureInfo
-dontwarn org.bouncycastle.asn1.cms.Attribute
-dontwarn org.bouncycastle.asn1.cms.AttributeTable
-dontwarn org.bouncycastle.asn1.cms.ContentInfo
-dontwarn org.bouncycastle.asn1.cms.EncryptedContentInfo
-dontwarn org.bouncycastle.asn1.cms.EnvelopedData
-dontwarn org.bouncycastle.asn1.cms.IssuerAndSerialNumber
-dontwarn org.bouncycastle.asn1.cms.KeyTransRecipientInfo
-dontwarn org.bouncycastle.asn1.cms.OriginatorInfo
-dontwarn org.bouncycastle.asn1.cms.RecipientIdentifier
-dontwarn org.bouncycastle.asn1.cms.RecipientInfo
-dontwarn org.bouncycastle.asn1.ess.ESSCertID
-dontwarn org.bouncycastle.asn1.ess.ESSCertIDv2
-dontwarn org.bouncycastle.asn1.ess.SigningCertificate
-dontwarn org.bouncycastle.asn1.ess.SigningCertificateV2
-dontwarn org.bouncycastle.asn1.ocsp.BasicOCSPResponse
-dontwarn org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers
-dontwarn org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
-dontwarn org.bouncycastle.asn1.tsp.MessageImprint
-dontwarn org.bouncycastle.asn1.tsp.TSTInfo
-dontwarn org.bouncycastle.asn1.x500.X500Name
-dontwarn org.bouncycastle.asn1.x509.AlgorithmIdentifier
-dontwarn org.bouncycastle.asn1.x509.CRLDistPoint
-dontwarn org.bouncycastle.asn1.x509.DistributionPoint
-dontwarn org.bouncycastle.asn1.x509.DistributionPointName
-dontwarn org.bouncycastle.asn1.x509.Extension
-dontwarn org.bouncycastle.asn1.x509.Extensions
-dontwarn org.bouncycastle.asn1.x509.GeneralName
-dontwarn org.bouncycastle.asn1.x509.GeneralNames
-dontwarn org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
-dontwarn org.bouncycastle.asn1.x509.TBSCertificateStructure
-dontwarn org.bouncycastle.cert.X509CertificateHolder
-dontwarn org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
-dontwarn org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
-dontwarn org.bouncycastle.cert.ocsp.BasicOCSPResp
-dontwarn org.bouncycastle.cert.ocsp.CertificateID
-dontwarn org.bouncycastle.cert.ocsp.CertificateStatus
-dontwarn org.bouncycastle.cert.ocsp.OCSPException
-dontwarn org.bouncycastle.cert.ocsp.OCSPReq
-dontwarn org.bouncycastle.cert.ocsp.OCSPReqBuilder
-dontwarn org.bouncycastle.cert.ocsp.OCSPResp
-dontwarn org.bouncycastle.cert.ocsp.SingleResp
-dontwarn org.bouncycastle.cms.CMSEnvelopedData
-dontwarn org.bouncycastle.cms.Recipient
-dontwarn org.bouncycastle.cms.RecipientId
-dontwarn org.bouncycastle.cms.RecipientInformation
-dontwarn org.bouncycastle.cms.RecipientInformationStore
-dontwarn org.bouncycastle.cms.SignerInformationVerifier
-dontwarn org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
-dontwarn org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
-dontwarn org.bouncycastle.cms.jcajce.JceKeyTransRecipient
-dontwarn org.bouncycastle.crypto.BlockCipher
-dontwarn org.bouncycastle.crypto.CipherParameters
-dontwarn org.bouncycastle.crypto.engines.AESFastEngine
-dontwarn org.bouncycastle.crypto.modes.CBCBlockCipher
-dontwarn org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
-dontwarn org.bouncycastle.crypto.params.KeyParameter
-dontwarn org.bouncycastle.crypto.params.ParametersWithIV
-dontwarn org.bouncycastle.jcajce.provider.digest.GOST3411$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.MD2$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.MD5$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.RIPEMD128$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.RIPEMD160$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.RIPEMD256$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.SHA1$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.SHA224$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.SHA256$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.SHA384$Digest
-dontwarn org.bouncycastle.jcajce.provider.digest.SHA512$Digest
-dontwarn org.bouncycastle.jce.X509Principal
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-dontwarn org.bouncycastle.jce.provider.X509CertParser
-dontwarn org.bouncycastle.ocsp.RevokedStatus
-dontwarn org.bouncycastle.operator.ContentVerifierProvider
-dontwarn org.bouncycastle.operator.DigestCalculator
-dontwarn org.bouncycastle.operator.DigestCalculatorProvider
-dontwarn org.bouncycastle.operator.OperatorCreationException
-dontwarn org.bouncycastle.operator.bc.BcDigestCalculatorProvider
-dontwarn org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
-dontwarn org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
-dontwarn org.bouncycastle.tsp.TimeStampRequest
-dontwarn org.bouncycastle.tsp.TimeStampRequestGenerator
-dontwarn org.bouncycastle.tsp.TimeStampResponse
-dontwarn org.bouncycastle.tsp.TimeStampToken
-dontwarn org.bouncycastle.tsp.TimeStampTokenInfo
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
