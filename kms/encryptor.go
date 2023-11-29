
package main

import (
	"bytes"
	"context"
	"encoding/base64"
	"fmt"
	"hash/crc32"
	"io"

	kms "cloud.google.com/go/kms/apiv1"
	"cloud.google.com/go/kms/apiv1/kmspb"
	"google.golang.org/protobuf/types/known/wrapperspb"
)

func main() {
	var buf bytes.Buffer
        err := encryptSymmetric(
		&buf, 
		"projects/ibcwe-event-layer-f3ccf6d9/locations/global/keyRings/fpe-poc-keyring/cryptoKeys/fpe-poc-key", 
                "mXC+RrazJKJTaEUQbJBZwQDu3ARhaB+FFmYxgV5aqS0=")
        if err != nil {
                fmt.Println(err)
        }
        fmt.Println(buf.String())

}
// encryptSymmetric encrypts the input plaintext with the specified symmetric
// Cloud KMS key.
func encryptSymmetric(w io.Writer, keyName string, message string) error {
	// keyName := "projects/my-project/locations/us-east1/keyRings/my-key-ring/cryptoKeys/my-key"
	// message := "Sample message"

	// Create the client.
	ctx := context.Background()
	client, err := kms.NewKeyManagementClient(ctx)
	if err != nil {
		return fmt.Errorf("failed to create kms client: %w", err)
	}
	defer client.Close()

	// Convert the message into bytes. Cryptographic plaintexts and
	// ciphertexts are always byte arrays.
	plaintext := []byte(message)

	// Optional but recommended: Compute plaintext's CRC32C.
	crc32c := func(data []byte) uint32 {
		t := crc32.MakeTable(crc32.Castagnoli)
		return crc32.Checksum(data, t)
	}
	plaintextCRC32C := crc32c(plaintext)

	// Build the request.
	req := &kmspb.EncryptRequest{
		Name:            keyName,
		Plaintext:       plaintext,
		PlaintextCrc32C: wrapperspb.Int64(int64(plaintextCRC32C)),
	}

	// Call the API.
	result, err := client.Encrypt(ctx, req)
	if err != nil {
		return fmt.Errorf("failed to encrypt: %w", err)
	}

	// Optional, but recommended: perform integrity verification on result.
	// For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
	// https://cloud.google.com/kms/docs/data-integrity-guidelines
	if result.VerifiedPlaintextCrc32C == false {
		return fmt.Errorf("Encrypt: request corrupted in-transit")
	}
	if int64(crc32c(result.Ciphertext)) != result.CiphertextCrc32C.Value {
		return fmt.Errorf("Encrypt: response corrupted in-transit")
	}

	b64 := make([]byte, base64.StdEncoding.DecodedLen(len(result.Ciphertext)))
	n, err := base64.StdEncoding.Decode(b64, result.Ciphertext)
	if err != nil {
		return err
	}


	// return result.Ciphertext, nil
	/*
	encodedCiphertext := fmt.Sprintf("%s", result.Ciphertext)
	decodedCiphertext, err := base64.StdEncoding.DecodeString(encodedCiphertext)

	if err != nil {
		return err
	}
	*/

	fmt.Fprintf(w, "Encrypted ciphertext: %s", b64[:n])
	return nil
}

func decryptSymmetric(w io.Writer, name string, ciphertext []byte) error {
	// name := "projects/my-project/locations/us-east1/keyRings/my-key-ring/cryptoKeys/my-key"
	// ciphertext := []byte("...")  // result of a symmetric encryption call

	// Create the client.
	ctx := context.Background()
	client, err := kms.NewKeyManagementClient(ctx)
	if err != nil {
		return fmt.Errorf("failed to create kms client: %w", err)
	}
	defer client.Close()

	// Optional, but recommended: Compute ciphertext's CRC32C.
	crc32c := func(data []byte) uint32 {
		t := crc32.MakeTable(crc32.Castagnoli)
		return crc32.Checksum(data, t)
	}
	ciphertextCRC32C := crc32c(ciphertext)

	// Build the request.
	req := &kmspb.DecryptRequest{
		Name:             name,
		Ciphertext:       ciphertext,
		CiphertextCrc32C: wrapperspb.Int64(int64(ciphertextCRC32C)),
	}

	// Call the API.
	result, err := client.Decrypt(ctx, req)
	if err != nil {
		return fmt.Errorf("failed to decrypt ciphertext: %w", err)
	}

	// Optional, but recommended: perform integrity verification on result.
	// For more details on ensuring E2E in-transit integrity to and from Cloud KMS visit:
	// https://cloud.google.com/kms/docs/data-integrity-guidelines
	if int64(crc32c(result.Plaintext)) != result.PlaintextCrc32C.Value {
		return fmt.Errorf("Decrypt: response corrupted in-transit")
	}

	fmt.Fprintf(w, "Decrypted plaintext: %s", result.Plaintext)
	return nil
}
