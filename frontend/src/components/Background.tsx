import React, {forwardRef} from "react";
import {twMerge} from "tailwind-merge";


export const DotBackground =
  forwardRef(({className, ...props}:
                React.HTMLProps<HTMLDivElement>, ref: React.ForwardedRef<HTMLDivElement>) => (
    <div
      className={twMerge('bg-white bg-[radial-gradient(#e5e7eb_2px,transparent_2px)] [background-size:16px_16px]', className)}
      ref={ref} {...props}
    />
  ))

export const GridBackground =
  forwardRef(({className, ...props}:
                React.HTMLProps<HTMLDivElement>, ref: React.ForwardedRef<HTMLDivElement>) => (
    <div
      className={twMerge('bg-white bg-[linear-gradient(to_right,#80808012_2px,transparent_2px),linear-gradient(to_bottom,#80808012_2px,transparent_2px)] bg-[size:24px_24px]', className)}
      ref={ref} {...props}
    />
  ))
