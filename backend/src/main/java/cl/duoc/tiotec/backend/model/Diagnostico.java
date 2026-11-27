package cl.duoc.tiotec.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Diagnóstico de hardware que la app envía desde la pantalla "Cuidado Tech".
 * Los nombres de los atributos coinciden con DiagnosticoHardwareRequestDto de la app,
 * así el JSON se convierte directo en este objeto.
 */
@Entity
@Table(name = "diagnosticos")
public class Diagnostico {

    @Id
    @NotBlank
    private String id;

    @NotBlank
    private String fechaRegistro;

    private long timestampMs;

    @Min(0)
    @Max(100)
    private int nivelBateria;

    private boolean estaCargando;
    private float temperaturaBateria;
    private int voltajeBateria;
    private String saludBateria;
    private String fuenteEnergia;
    private double almacenamientoTotalGb;
    private double almacenamientoLibreGb;
    private int porcentajeAlmacenamientoUsado;
    private long ramTotalMb;
    private long ramDisponibleMb;
    private int totalSensores;
    private double consumoSensoresMa;

    @NotBlank
    private String nivelRiesgo;

    private String recomendacion;

    /** Todo diagnóstico que está en el servidor ya quedó sincronizado. */
    public boolean isSincronizadoRemoto() {
        return true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public long getTimestampMs() { return timestampMs; }
    public void setTimestampMs(long timestampMs) { this.timestampMs = timestampMs; }

    public int getNivelBateria() { return nivelBateria; }
    public void setNivelBateria(int nivelBateria) { this.nivelBateria = nivelBateria; }

    public boolean isEstaCargando() { return estaCargando; }
    public void setEstaCargando(boolean estaCargando) { this.estaCargando = estaCargando; }

    public float getTemperaturaBateria() { return temperaturaBateria; }
    public void setTemperaturaBateria(float temperaturaBateria) { this.temperaturaBateria = temperaturaBateria; }

    public int getVoltajeBateria() { return voltajeBateria; }
    public void setVoltajeBateria(int voltajeBateria) { this.voltajeBateria = voltajeBateria; }

    public String getSaludBateria() { return saludBateria; }
    public void setSaludBateria(String saludBateria) { this.saludBateria = saludBateria; }

    public String getFuenteEnergia() { return fuenteEnergia; }
    public void setFuenteEnergia(String fuenteEnergia) { this.fuenteEnergia = fuenteEnergia; }

    public double getAlmacenamientoTotalGb() { return almacenamientoTotalGb; }
    public void setAlmacenamientoTotalGb(double almacenamientoTotalGb) { this.almacenamientoTotalGb = almacenamientoTotalGb; }

    public double getAlmacenamientoLibreGb() { return almacenamientoLibreGb; }
    public void setAlmacenamientoLibreGb(double almacenamientoLibreGb) { this.almacenamientoLibreGb = almacenamientoLibreGb; }

    public int getPorcentajeAlmacenamientoUsado() { return porcentajeAlmacenamientoUsado; }
    public void setPorcentajeAlmacenamientoUsado(int porcentajeAlmacenamientoUsado) { this.porcentajeAlmacenamientoUsado = porcentajeAlmacenamientoUsado; }

    public long getRamTotalMb() { return ramTotalMb; }
    public void setRamTotalMb(long ramTotalMb) { this.ramTotalMb = ramTotalMb; }

    public long getRamDisponibleMb() { return ramDisponibleMb; }
    public void setRamDisponibleMb(long ramDisponibleMb) { this.ramDisponibleMb = ramDisponibleMb; }

    public int getTotalSensores() { return totalSensores; }
    public void setTotalSensores(int totalSensores) { this.totalSensores = totalSensores; }

    public double getConsumoSensoresMa() { return consumoSensoresMa; }
    public void setConsumoSensoresMa(double consumoSensoresMa) { this.consumoSensoresMa = consumoSensoresMa; }

    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }

    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }
}
