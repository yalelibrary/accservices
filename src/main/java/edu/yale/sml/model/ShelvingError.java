package edu.yale.sml.model;

public class ShelvingError implements java.io.Serializable
{

	private static final long serialVersionUID = -1201513663541649150L;
	int accuracy_errors = 0;
	int discharge_errors = 0;
	int enum_warnings = 0;
	int location_errors = 0;
	int null_barcodes = 0;
	int null_result_barcodes = 0;
	int oversize_errors = 0;
	int total_errors = 0;
	int status_errors = 0;
	int suppress_errors = 0;
	int misshelf_errors = 0;
	int misshelf_threshold_errors = 0;

	public int getMisshelf_threshold_errors()
    {
        return misshelf_threshold_errors;
    }

    public void setMisshelf_threshold_errors(int misshelf_threshold_erros)
    {
        this.misshelf_threshold_errors = misshelf_threshold_erros;
    }

    public int getMisshelf_errors()
	{
		return misshelf_errors;
	}

	public void setMisshelf_errors(int misshelf_errors)
	{
		this.misshelf_errors = misshelf_errors;
	}

	public ShelvingError()
	{
	}

	public ShelvingError(int accuracy_errors, int discharge_errors,
			int enum_warnings, int null_barcodes, int null_result_barcodes,
			int oversize_errors, int total_errors, int location_errors,
			int status_errors, int suppress_errors)
	{
		super();
		this.accuracy_errors = accuracy_errors;
		this.discharge_errors = discharge_errors;
		this.enum_warnings = enum_warnings;
		this.null_barcodes = null_barcodes;
		this.null_result_barcodes = null_result_barcodes;
		this.oversize_errors = oversize_errors;
		this.total_errors = total_errors;
		this.location_errors = location_errors;
		this.status_errors = status_errors;
		this.suppress_errors = suppress_errors;
	}

	public int getSuppress_errors()
	{
		return suppress_errors;
	}

	public void setSuppress_errors(int suppress_errors)
	{
		this.suppress_errors = suppress_errors;
	}

	public int getStatus_errors()
	{
		return status_errors;
	}

	public void setStatus_errors(int status_errors)
	{
		this.status_errors = status_errors;
	}

	public int getAccuracy_errors()
	{
		return accuracy_errors;
	}

	public int getDischarge_errors()
	{
		return discharge_errors;
	}

	public int getEnum_warnings()
	{
		return enum_warnings;
	}

	public int getLocation_errors()
	{
		return location_errors;
	}

	public int getNull_barcodes()
	{
		return null_barcodes;
	}

	public int getNull_result_barcodes()
	{
		return null_result_barcodes;
	}

	public int getOversize_errors()
	{
		return oversize_errors;
	}

	public int getTotal_errors()
	{
		return total_errors;
	}

	public void setAccuracy_errors(int accuracy_errors)
	{
		this.accuracy_errors = accuracy_errors;
	}

	public void setDischarge_errors(int discharge_errors)
	{
		this.discharge_errors = discharge_errors;
	}

	public void setEnum_warnings(int enum_warnings)
	{
		this.enum_warnings = enum_warnings;
	}

	public void setLocation_errors(int location_errors)
	{
		this.location_errors = location_errors;
	}

	public void setNull_barcodes(int null_barcodes)
	{
		this.null_barcodes = null_barcodes;
	}

	public void setNull_result_barcodes(int null_result_barcodes)
	{
		this.null_result_barcodes = null_result_barcodes;
	}

	public void setOversize_errors(int oversize_errors)
	{
		this.oversize_errors = oversize_errors;
	}

	public void setTotal_errors(int total_errors)
	{
		this.total_errors = total_errors;
	}
}
